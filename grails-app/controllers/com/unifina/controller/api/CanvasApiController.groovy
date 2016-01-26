package com.unifina.controller.api

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.log4j.Logger

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasApiController {

	def canvasService
	def springSecurityService
	def grailsApplication
	def unifinaSecurityService

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

	@StreamrApi
	def show(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
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
		getAuthorizedCanvas(id) { Canvas canvas ->
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
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot delete common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvas.delete(flush: true)
			}
		}
	}

	@StreamrApi
	def start(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot start common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvasService.start(canvas, request.JSON?.clearState ?: false)
				render canvas.toMap() as JSON
			}
		}
	}

	@StreamrApi
	def stop(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text:[error: "cannot stop common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvasService.stop(canvas)
				canvas.refresh() // Canvas updated in separate thread
				render canvas.toMap() as JSON
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

	private void getAuthorizedCanvas(String id, Closure successHandler) {
		def canvas = Canvas.get(id)
		if (canvas == null) {
			render(status: 404, text: [error: "Canvas (id=$id) not found.", code: "NOT_FOUND"] as JSON)
		} else if (!canvas.example && !unifinaSecurityService.canAccess(canvas, actionName == 'load', request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Canvas (id=$id)", code: "FORBIDDEN"] as JSON)
		} else {
			successHandler.call(canvas)
		}
	}
}