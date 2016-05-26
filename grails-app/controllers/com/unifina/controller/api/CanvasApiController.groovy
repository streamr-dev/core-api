package com.unifina.controller.api

import com.unifina.api.SaveCanvasCommand
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
	def signalPathService
	def permissionService
	def apiService

	private static final Logger log = Logger.getLogger(CanvasApiController)

	@StreamrApi
	def index() {
		def criteria = apiService.createListCriteria(params, ["name"], {
			// Filter by exact name
			if (params.name) {
				eq "name", params.name
			}
			// Filter by adhoc
			if (params.adhoc) {
				eq "adhoc", params.boolean("adhoc")
			}
			// Filter by state
			if (params.state) {
				eq "state", Canvas.State.fromValue(params.state)
			}
		})
		def canvases = permissionService.get(Canvas, request.apiUser, Operation.READ, apiService.isPublicFlagOn(params), criteria)
		render(canvases*.toMap() as JSON)
	}

	@StreamrApi(requiresAuthentication = false)
	def show(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.READ)
		Map result = canvasService.reconstruct(canvas)
		canvas.json = result as JSON
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def save() {
		Canvas canvas = canvasService.createNew(readSaveCommand(), request.apiUser)
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def update(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
		canvasService.updateExisting(canvas, readSaveCommand())
		render canvas.toMap() as JSON
	}

	@StreamrApi
	def delete(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
		canvas.delete(flush: true)
		response.status = 204
		render ""
	}

	@StreamrApi
	def start(String id) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.WRITE)
		canvasService.start(canvas, request.JSON?.clearState ?: false)
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
	 * Sends a runtime request to a running canvas
	 */
	@StreamrApi(requiresAuthentication = false)
	def request(String id, Boolean local) {
		Canvas canvas = canvasService.authorizedGetById(id, request.apiUser, Operation.READ)
		def msg = request.JSON
		Map response = signalPathService.runtimeRequest(msg, canvas, null, request.apiUser, local ? true : false)

		log.info("request: responding with $response")
		render response as JSON
	}

	/**
	 * Gets the json of a single module on a canvas
	 */
	@StreamrApi(requiresAuthentication = false)
	def module(String canvasId, Integer moduleId, Long dashboard) {
		Map moduleMap = canvasService.authorizedGetModuleOnCanvas(canvasId, moduleId, dashboard, request.apiUser, Operation.READ)
		render moduleMap as JSON
	}

	/**
	 * Sends a runtime request to a module on a canvas
     */
	@StreamrApi(requiresAuthentication = false)
	def moduleRequest(String canvasId, Integer moduleId, Long dashboard, Boolean local) {
		// Always asks for read permission only. Problem?
		Map moduleMap = canvasService.authorizedGetModuleOnCanvas(canvasId, moduleId, dashboard, request.apiUser, Operation.READ)
		Canvas canvas = Canvas.get(canvasId)
		def msg = request.JSON

		Map response = signalPathService.runtimeRequest(msg, canvas, moduleId, request.apiUser, local ? true : false)
		log.info("request: responding with $response")
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