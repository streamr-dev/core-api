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

	def signalPathService
	def springSecurityService
	def grailsApplication
	def unifinaSecurityService

	private static final Logger log = Logger.getLogger(CanvasApiController)

	@StreamrApi
	def index() {
		List<Canvas> canvases
		if (request.query) {
			canvases = Canvas.findAllByUserAndName(request.apiUser, query)
		} else {
			canvases = Canvas.findAllByUser(request.apiUser)
		}
		render(canvases.collect { it.toMap() } as JSON)
	}

	@StreamrApi
	def show(long id) {
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
	def update(long id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.type == Canvas.Type.EXAMPLE) {
				render(status: 403, text: [error: "cannot update common example", code: "FORBIDDEN"] as JSON)
			} else {
				readAndSave(canvas, params.json)
			}
		}
	}

	@StreamrApi
	def save() {
		readAndSave(new Canvas(), params.json)
	}

	@StreamrApi
	def delete(long id) {
		getAuthorizedCanvas(id) { Canvas canvas ->
			if (canvas.type == Canvas.Type.EXAMPLE) {
				render(status: 403, text:[error: "cannot delete common example", code: "FORBIDDEN"] as JSON)
			} else {
				canvas.delete(flush: true)
			}
		}
	}

	private void readAndSave(Canvas canvas, Map signalPathMap) {
		Globals globals = GlobalsFactory.createInstance(signalPathMap.settings ?: [:], grailsApplication)

		try {
			// Rebuild the json to check it's ok and up to date
			def signalPathAsMap = signalPathService.reconstruct(signalPathMap, globals)
			def signalPathAsJson = (signalPathAsMap as JSON)

			canvas.name = signalPathAsMap.name
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

	private void getAuthorizedCanvas(long id, Closure successHandler) {
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
