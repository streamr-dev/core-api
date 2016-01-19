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
				readAndSave(canvas)
			}
		}
	}

	@StreamrApi
	def save() {
		// TODO: if type = running, create uiChannel
		readAndSave(new Canvas())
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

	private void readAndSave(Canvas canvas) {
		Globals globals = GlobalsFactory.createInstance(request.JSON.settings ?: [:], grailsApplication)

		try {
			// Rebuild the json to check it's ok and up to date
			def signalPathAsMap = signalPathService.reconstruct(request.JSON, globals)
			def signalPathAsJson = (signalPathAsMap as JSON)

			canvas.name = signalPathAsMap.name
			canvas.type = Canvas.Type.valueOf(request.JSON.type.toUpperCase())
			canvas.adhoc = request.JSON.adhoc
			canvas.json = signalPathAsJson
			canvas.hasExports = signalPathAsMap.hasExports
			canvas.user = request.apiUser
			canvas.save(flush: true, failOnError: true)

			render canvas.toMap() as JSON
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Save failed", e)
			render(
				status: 500,
				text: [
					error: message(code: "signalpath.save.error", args: [e.message]),
					code : "FAILED_TO_SAVE_SIGNAL_PATH"
				] as JSON
			)
		} finally {
			globals.destroy()
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
