package com.unifina.controller.api

import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.security.StreamrApi
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.GrailsUtil
import org.apache.log4j.Logger

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasesApiController {

	def signalPathService
	def springSecurityService
	def grailsApplication
	def unifinaSecurityService

	private static final Logger log = Logger.getLogger(CanvasesApiController)

	def createSaveData(SavedSignalPath ssp) {
		return [
			isSaved: true,
			url: createLink(controller:"canvasesApi", action:"save", params:[id:ssp.id]),
			name: ssp.name,
			target: "Archive id "+ssp.id
		]
	}

	@StreamrApi
	def index() {
		List<SavedSignalPath> savedSignalPaths
		if (request.query) {
			savedSignalPaths = SavedSignalPath.findAllByUserAndName(request.apiUser, query)
		} else {
			savedSignalPaths = SavedSignalPath.findAllByUser(request.apiUser)
		}
		render(savedSignalPaths.collect { it.toMap() } as JSON)
	}

	@StreamrApi
	def load() {
		SavedSignalPath ssp = SavedSignalPath.get(Integer.parseInt(params.id))
		if (ssp == null) {
			render(status: 404, text: [error: "Canvas with id $params.id not found.", code: "NOT_FOUND"] as JSON)
			return
		} else if (!unifinaSecurityService.canAccess(ssp, actionName == 'load', request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Canvas " + params.id, code: "FORBIDDEN"] as JSON)
			return
		}

		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)

		Map json = JSON.parse(ssp.json)

		// Reconstruct to bring the path up to date
		json.signalPathData.name = ssp.name

		Map result = json

		try {
			result = signalPathService.reconstruct(json, globals)
		} catch (Throwable e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error loading SignalPath", e)
			result.error = true
			result.message = message(code: "signalpath.load.error", args: [e.message])
		} finally {
			// Examples can not be saved in place by others than those who have real access to it
			if (ssp.type != SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH || unifinaSecurityService.canAccess(ssp, request.apiUser))
				result.saveData = createSaveData(ssp)

			render result as JSON
			globals.destroy()
		}
	}

	@StreamrApi
	def save() {
		SavedSignalPath ssp = new SavedSignalPath()
		if (params.id) {
			ssp = SavedSignalPath.get(Integer.parseInt(params.id))
			if (ssp == null) {
				render(status: 404, text: [error: "Canvas with id $params.id not found.", code: "NOT_FOUND"] as JSON)
				return
			} else if (!unifinaSecurityService.canAccess(ssp, actionName == 'load', request.apiUser)) {
				render(status: 403, text: [error: "Not authorized to access Canvas " + params.id, code: "FORBIDDEN"] as JSON)
				return
			}
		}

		def settings = params.json.settings ?: [:]
		Globals globals = GlobalsFactory.createInstance(settings, grailsApplication)

		try {
			// Rebuild the json to check it's ok and up to date
			def signalPathAsMap = signalPathService.reconstruct(params.json, globals)
			signalPathAsMap.uuid = ssp.uuid

			def signalPathAsJson = (signalPathAsMap as JSON)

			ssp.name = signalPathAsMap.name
			ssp.json = signalPathAsJson
			ssp.hasExports = signalPathAsMap.hasExports
			ssp.user = request.apiUser
			ssp.save(flush: true, failOnError: true)


			render signalPathAsJson
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Save failed", e)
			Map r = [
				error  : true,
				message: message(code: "signalpath.save.error", args: [e.message])
			]
			render r as JSON
		} finally {
			globals.destroy()
		}
	}
}
