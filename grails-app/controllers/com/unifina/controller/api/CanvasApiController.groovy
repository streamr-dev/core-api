package com.unifina.controller.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.unifina.api.*
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.AllowRole
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.signalpath.ModuleException
import com.unifina.utils.NullJsonSerializer
import grails.converters.JSON
import grails.transaction.NotTransactional
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.util.FileCopyUtils

class CanvasApiController {

	CanvasService canvasService
	SignalPathService signalPathService
	ApiService apiService

	private static final Gson gson = new GsonBuilder()
		.serializeNulls()
		.registerTypeAdapter(JSONObject.Null, new NullJsonSerializer())
		.create()

	private static final Logger log = Logger.getLogger(CanvasApiController)

	@StreamrApi
	def index(CanvasListParams listParams) {
		if (params.public != null) {
			listParams.publicAccess = params.boolean("public")
		}
		def results = apiService.list(Canvas, listParams, (SecUser) request.apiUser)
		apiService.addLinkHintToHeader(listParams, results.size(), params, response)
		render(results*.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id, Boolean runtime) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.CANVAS_GET)
		if (runtime) {
			Map result = canvas.toMap()
			Map runtimeJson = signalPathService.runtimeRequest(signalPathService.buildRuntimeRequest([type: 'json'], "canvases/$canvas.id", request.apiUser), false).json
			result.putAll(runtimeJson)
			render result as JSON
		}
		else {
			try {
				Map result = canvasService.reconstruct(canvas, request.apiUser)
				// Need to discard this change below to prevent auto-update
				canvas.json = result as JSON
				render canvas.toMap() as JSON
				// Prevent auto-update of the canvas
				canvas.discard()
			} catch (ModuleException e) {
				// Load canvas even if it is in an invalid state. For front-end auto-save.
				Map<String, Object> response = canvas.toMap()
				response.moduleErrors = e.getModuleExceptions()*.toMap()
				render response as JSON
			}
		}
	}

	@StreamrApi
	def save() {
		Canvas canvas = canvasService.createNew(readSaveCommand(), request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def update(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.CANVAS_EDIT)
		try {
			canvasService.updateExisting(canvas, readSaveCommand(), request.apiUser)
		} catch (ModuleException e) {
			Map<String, Object> response = canvas.toMap()
			response.moduleErrors = e.getModuleExceptions()*.toMap()
			render gson.toJson(response)
			return
		}
		render gson.toJson(canvas.toMap())
	}

	@StreamrApi
	def delete(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.CANVAS_DELETE)
		canvasService.deleteCanvas(canvas, request.apiUser)
		response.status = 204
		render ""
	}

	@StreamrApi
	def start(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.CANVAS_STARTSTOP)
		canvasService.start(canvas, request.JSON?.clearState ?: false, request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER, allowRoles = AllowRole.ADMIN)
	def startAsAdmin(String id, StartCanvasAsAdminParams adminParams) {
		if (!adminParams.validate()) {
			throw new ValidationException(adminParams.errors)
		}

		Canvas canvas = apiService.getByIdAndThrowIfNotFound(Canvas, id)
		canvasService.start(canvas, request.JSON?.clearState ?: false, adminParams.startedBy)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	@NotTransactional
	def stop(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.CANVAS_STARTSTOP)
		// Updates canvas in another thread, so canvas needs to be refreshed
		canvasService.stop(canvas, request.apiUser)
		if (canvas.adhoc) {
			response.status = 204 // Adhoc canvases are deleted on stop.
			render ""
		} else {
			canvas.refresh()
			render canvas.toMap() as JSON
		}
	}

	/**
	 * Gets the json of a single module on a canvas
	 */
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def module(String canvasId, Integer moduleId, String dashboardId, Boolean runtime) {
		if (runtime) {
			render signalPathService.runtimeRequest(signalPathService.buildRuntimeRequest([type: 'json'], "canvases/$canvasId/modules/$moduleId", request.apiUser), false).json as JSON
		} else {
			Map moduleMap = canvasService.authorizedGetModuleOnCanvas(canvasId, moduleId, dashboardId, request.apiUser, Operation.CANVAS_GET)
			render moduleMap as JSON
		}
	}

	/**
	 * Sends a runtime request to a running canvas or module
     */
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def runtimeRequest(String path, Boolean local) {
		def msg = request.JSON
		Map response = signalPathService.runtimeRequest(signalPathService.buildRuntimeRequest(msg, "canvases/$path", request.apiUser), local ? true : false)
		log.debug("request: responding with $response")
		render response as JSON
	}

	private SaveCanvasCommand readSaveCommand() {

		// Ideally, this would be done straight in parameter lists of actions thereby implicitly binding data.
		// Unfortunately Grails uses Google's GSON to deserialize data which doesn't fully deserialize integers but
		// instead leaves them as "LazilyParsedNumber"(s) which cannot be cast to type Integer.

		def command = new SaveCanvasCommand()
		bindData(command, request.JSON)
		return command
	}

	boolean validateFilename(String filename) {
		if (filename == null) {
			return false
		}
		return filename ==~ /^streamr_csv_[0-9]{1,19}\.csv$/
	}

	@StreamrApi
	def downloadCsv() {
		if (!validateFilename(params.filename)) {
			throw new BadRequestException("INVALID_PARAMETER", "filename contains illegal characters")
		}
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + params.filename
		File file = new File(fileName)
		if (file.canRead()) {
			FileInputStream fileInputStream = new FileInputStream(file)
			response.setContentType("text/csv")
			response.setHeader("Content-Disposition", "attachment; filename=" + file.name);
			response.setHeader("Content-Length", file.length() + "")
			FileCopyUtils.copy(fileInputStream, response.outputStream)
			fileInputStream.close()
			file.delete()
		} else {
			throw new NotFoundException("File not found: " + params.filename)
		}
	}
}
