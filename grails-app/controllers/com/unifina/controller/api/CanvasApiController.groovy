package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.NotTransactional
import org.apache.log4j.Logger

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasApiController {

	def canvasService
	def springSecurityService
	def grailsApplication
	def permissionService
	def signalPathService

	private static final Logger log = Logger.getLogger(CanvasApiController)

	@StreamrApi
	def index() {
		def criteria = StreamrApiHelper.createListCriteria(params, ["name"], {
			// Lookup by exact name
			if (params.name) {
				eq "name", params.name
			}
			// Lookup by adhoc
			if (params.adhoc) {
				eq "adhoc", params.boolean("adhoc")
			}
			// Lookup by state
			if (params.state) {
				eq "state", Canvas.State.fromValue(params.state)
			}
		})
		def canvases = permissionService.get(Canvas, request.apiUser, Operation.READ, StreamrApiHelper.isPublicFlagOn(params), criteria)
		render(canvases*.toMap() as JSON)
	}

	@StreamrApi(requiresAuthentication = false)
	def show(String id) {
		getAuthorizedCanvas(id, Operation.READ) { Canvas canvas ->
			Map result = canvasService.reconstruct(canvas)
			canvas.json = result as JSON
			render canvas.toMap() as JSON
		}
	}

	@StreamrApi
	def save() {
		Canvas canvas = canvasService.createNew(readSaveCommand(), request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def update(String id) {
		getAuthorizedCanvas(id, Operation.WRITE) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text: [error: "cannot update common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvasService.updateExisting(canvas, readSaveCommand())
				render canvas.toMap() as JSON
			}
		}
	}

	@StreamrApi
	def delete(String id) {
		getAuthorizedCanvas(id, Operation.WRITE) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot delete common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvas.delete(flush: true)
			}
		}
	}

	@StreamrApi
	def start(String id) {
		getAuthorizedCanvas(id, Operation.WRITE) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot start common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvasService.start(canvas, request.JSON?.clearState ?: false)
				render canvas.toMap() as JSON
			}
		}
	}

	@StreamrApi
	@NotTransactional
	def stop(String id) {
		getAuthorizedCanvas(id, Operation.WRITE) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot stop common example", code: "FORBIDDEN"] as JSON)
			} else {
				// Updates canvas in another thread, so canvas needs to be refreshed
				canvasService.stop(canvas, request.apiUser)
				if (canvas.adhoc) {
					render(status: 204) // Adhoc canvases are deleted on stop.
				} else {
					canvas.refresh()
					render canvas.toMap() as JSON
				}
			}
		}
	}

	/**
	 * Gets the json of a single module on a canvas
	 * @param id
	 * @param moduleId
     * @return
     */
	@StreamrApi(requiresAuthentication = false)
	def module(String id, Integer moduleId) {
		getAuthorizedCanvas(id, Operation.READ) { Canvas canvas ->
			Map canvasMap = canvas.toMap()
			Map moduleMap = canvasMap.modules.find { it.hash.toString() == moduleId.toString() }

			if (!moduleMap) {
				throw new ApiException(404, "MODULE_NOT_FOUND", "Module $moduleId not found on canvas $id")
			} else {
				render moduleMap as JSON
			}

		}
	}

	@StreamrApi(requiresAuthentication = false)
	def request(String id, Integer moduleId) {
		getAuthorizedCanvas(id, Operation.READ) { Canvas canvas ->
			def msg = request.JSON
			Map response = signalPathService.runtimeRequest(msg, canvas, moduleId, request.apiUser, params.local ? true : false)

			log.info("request: responding with $response")
			render response as JSON
		}
	}

	private SaveCanvasCommand readSaveCommand() {

		// Ideally, this would be done straight in parameter lists of actions thereby implicitly binding data.
		// Unfortunately Grails uses Google's GSON to deserialize data which doesn't fully deserialize integers but
		// instead leaves them as "LazilyParsedNumber"(s) which cannot be cast to type Integer.

		def command = new SaveCanvasCommand()
		bindData(command, request.JSON)
		return command
	}

	private void getAuthorizedCanvas(String id, Operation op, Closure action) {
		def canvas = Canvas.get(id)
		if (!canvas) {
			throw new NotFoundException("Canvas", id)
		} else if (!(op == Operation.READ && canvas.example) &&
				   !permissionService.check(request.apiUser, canvas, op)) {
			throw new NotPermittedException(request.apiUser?.username, "Canvas", id, op.id)
		} else {
			action.call(canvas)
		}
	}
}