package com.unifina.controller.api

import com.unifina.api.CanvasListParams
import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.NotTransactional
import org.apache.log4j.Logger

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasApiController {

	CanvasService canvasService
	SignalPathService signalPathService
	ApiService apiService

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
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.READ)
		if (runtime) {
			Map result = canvas.toMap()
			Map runtimeJson = signalPathService.runtimeRequest(signalPathService.buildRuntimeRequest([type: 'json'], "canvases/$canvas.id", request.apiUser), false).json
			result.putAll(runtimeJson)
			render result as JSON
		}
		else {
			Map result = canvasService.reconstruct(canvas, request.apiUser)
			// Need to discard this change below to prevent auto-update
			canvas.json = result as JSON
			render canvas.toMap() as JSON
			// Prevent auto-update of the canvas
			canvas.discard()
		}
	}

	@StreamrApi
	def save() {
		Canvas canvas = canvasService.createNew(readSaveCommand(), request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def update(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
		canvasService.updateExisting(canvas, readSaveCommand(), request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def delete(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
		canvasService.deleteCanvas(canvas, request.apiUser)
		response.status = 204
		render ""
	}

	@StreamrApi
	def start(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
		canvasService.start(canvas, request.JSON?.clearState ?: false, request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	@NotTransactional
	def stop(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
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
			Map moduleMap = canvasService.authorizedGetModuleOnCanvas(canvasId, moduleId, dashboardId, request.apiUser, Operation.READ)
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

}