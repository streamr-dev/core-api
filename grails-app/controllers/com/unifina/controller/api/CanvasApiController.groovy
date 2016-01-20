package com.unifina.controller.api

import com.unifina.domain.signalpath.Canvas
import com.unifina.security.StreamrApi
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.GrailsUtil
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
			params.type ? Canvas.Type.valueOf(params.type.toUpperCase()) : null,
			params.state ? Canvas.State.valueOf(params.state.toUpperCase()) : null
		)
		render(canvases*.toMap() as JSON)
	}

	@StreamrApi
	def show(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->

			Map signalPathMap = JSON.parse(canvas.json)
			def settings = signalPathMap.settings ?: [:]
			Globals globals = GlobalsFactory.createInstance(settings, grailsApplication)

			try {
				def result = signalPathService.reconstruct(signalPathMap, globals)
				canvas.json = result as JSON
				render canvas.toMap() as JSON
			} catch (Throwable e) {
				e = GrailsUtil.deepSanitize(e)
				log.error("Error loading SignalPath", e)
				render(
					status: 500,
					text: [
						error: message(code: "signalpath.load.error", args: [e.message]),
						code : "FAILED_TO_LOAD_SIGNAL_PATH"
					] as JSON
				)
			} finally {
				globals.destroy()
			}
		}
	}

	@StreamrApi
	def update(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.type == Canvas.Type.EXAMPLE) {
				render(status: 403, text: [error: "cannot update common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvasService.updateExisting(canvas, request.JSON, request.apiUser)
				render canvas as JSON
			}
		}
	}

	@StreamrApi
	def save() {
		// TODO: if type = running, create uiChannel
		Canvas canvas = canvasService.createNew(request.JSON, request.apiUser)
		render canvas as JSON
	}

	@StreamrApi
	def delete(String id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.type == Canvas.Type.EXAMPLE) {
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
		} else if (canvas.type != Canvas.Type.EXAMPLE &&
			!unifinaSecurityService.canAccess(canvas, actionName == 'load', request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Canvas (id=$id)", code: "FORBIDDEN"] as JSON)
		} else {
			successHandler.call(canvas)
		}
	}
}