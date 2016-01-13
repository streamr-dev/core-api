package com.unifina.controller.api

import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.security.StreamrApi
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
		SavedSignalPath ssp = getAuthorizedSavedSignalPath(params.id)
		if (ssp == null) {
			return
		}

		Map signalPathMap = JSON.parse(ssp.json)
		def settings = signalPathMap.settings ?: [:]
		Globals globals = GlobalsFactory.createInstance(settings, grailsApplication)

		try {
			def result = signalPathService.reconstruct(signalPathMap, globals)
			render ssp.toMap() as JSON
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

	@StreamrApi
	def save() {
		SavedSignalPath ssp = new SavedSignalPath()
		if (params.id) {
			ssp = getAuthorizedSavedSignalPath(params.id)
			if (ssp == null) {
				return
			}
		}

		def settings = params.json.settings ?: [:]
		Globals globals = GlobalsFactory.createInstance(settings, grailsApplication)

		try {
			// Rebuild the json to check it's ok and up to date
			def signalPathAsMap = signalPathService.reconstruct(params.json, globals)
			def signalPathAsJson = (signalPathAsMap as JSON)

			ssp.name = signalPathAsMap.name
			ssp.json = signalPathAsJson
			ssp.hasExports = signalPathAsMap.hasExports
			ssp.user = request.apiUser
			ssp.save(flush: true, failOnError: true)

			render ssp.toMap() as JSON
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

	private SavedSignalPath getAuthorizedSavedSignalPath(String id) {
		def ssp = SavedSignalPath.get(Integer.parseInt(params.id))
		if (ssp == null) {
			render(status: 404, text: [error: "Canvas with id $params.id not found.", code: "NOT_FOUND"] as JSON)
		} else if (!unifinaSecurityService.canAccess(ssp, actionName == 'load', request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Canvas " + params.id, code: "FORBIDDEN"] as JSON)
		} else {
			return ssp
		}
		return null
	}
}
