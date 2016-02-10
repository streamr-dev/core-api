package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.SecUser
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

	private static final Logger log = Logger.getLogger(CanvasApiController)

	@StreamrApi
	def index() {
		SecUser user = request.apiUser
		String name = params.name
		Boolean adhoc = params.boolean("adhoc")
		Canvas.State state = Canvas.State.fromValue(params.state)

		def canvases = canvasService.findAllBy(user, name, adhoc, state)
		render(canvases*.toMap() as JSON)
	}

	// TODO: /canvases/{id}/uiChannels (webcomponent?)

	@StreamrApi(requiresAuthentication = false)
	def show(String id) {
		getCanvasWithAccess(id, Operation.READ) { Canvas canvas ->
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
		getCanvasWithAccess(id, Operation.WRITE) { Canvas canvas ->
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
		getCanvasWithAccess(id, Operation.WRITE) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot delete common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvas.delete(flush: true)
			}
		}
	}

	@StreamrApi
	def start(String id) {
		getCanvasWithAccess(id, Operation.WRITE) { Canvas canvas ->
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
		getCanvasWithAccess(id, Operation.WRITE) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot stop common example", code: "FORBIDDEN"] as JSON)
			} else {
				// Updates canvas in another thread, so canvas needs to be refreshed
				try {
					canvasService.stop(canvas, request.apiUser)
				} catch (ApiException e) {
					render(status: e.statusCode, text: e.toMap() as JSON)
					return
				}

				try {
					// Adhoc canvases are deleted on stop, in which case refresh() will fail with UnresolvableObjectException
					canvas.refresh()
					render canvas.toMap() as JSON
				} catch (UnresolvableObjectException) {
					render(status: 204)
				}
			}
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

	private void getCanvasWithAccess(String id, Operation op, Closure successHandler) {
		def canvas = Canvas.get(id)
		if (!canvas) {
			render(status: 404, text: [error: "Canvas (id=$id) not found.", code: "NOT_FOUND"] as JSON)
		} else if (!permissionService.check(request.apiUser, canvas, op) && !(op == Operation.READ && canvas.example)) {
			render(status: 403, text: [error: "Not authorized to ${op.id} Canvas (id=$id)", code: "FORBIDDEN", fault: "op", op: op.id] as JSON)
		} else {
			successHandler.call(canvas)
		}
	}
}