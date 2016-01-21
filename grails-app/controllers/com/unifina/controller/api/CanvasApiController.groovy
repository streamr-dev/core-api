package com.unifina.controller.api

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.log4j.Logger

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasApiController {

	def canvasService
	def signalPathService
	def springSecurityService
	def grailsApplication
	def unifinaSecurityService

	private static final Logger log = Logger.getLogger(CanvasApiController)

	@StreamrApi
	def index() {
		List<Canvas> canvases = canvasService.findAllBy(
			request.apiUser,
			params.name,
			params.boolean("adhoc"),
			params.state ? Canvas.State.valueOf(params.state.toUpperCase()) : null
		)
		render(canvases*.toMap() as JSON)
	}

	@StreamrApi
	def show(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			Map result = canvasService.reconstruct(canvas)
			canvas.json = result as JSON
			render canvas.toMap() as JSON
		}
	}

	@StreamrApi
	def save(SaveCanvasCommand command) {
		Canvas canvas = canvasService.createNew(command, request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def update(String id, SaveCanvasCommand command) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.example) {
				render(status: 403, text: [error: "cannot update common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvasService.updateExisting(canvas, command)
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