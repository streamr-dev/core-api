package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.security.StreamrApi
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.GrailsUtil
import groovy.transform.CompileStatic
import org.apache.log4j.Logger

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasApiController {

	def signalPathService
	def springSecurityService
	def grailsApplication
	def unifinaSecurityService

	private static final Logger log = Logger.getLogger(CanvasApiController)

	def createSaveData(SavedSignalPath ssp) {
		return [
			isSaved: true,
			url: createLink(controller:"canvasApi", action:"save", params:[id:ssp.id]),
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
	def show(long id) {
		SavedSignalPath ssp = getAuthorizedSavedSignalPath(id)
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
	def update(long id) {
		SavedSignalPath ssp = getAuthorizedSavedSignalPath(id)
		if (ssp != null) {
			if (ssp.type == SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH) {
				render(status: 403, text:[error: "cannot update common example", code: "FORBIDDEN"] as JSON)
			} else {
			readAndSave(ssp, params.json, request.apiUser)
			}
		}
	}

	@StreamrApi
	def save() {
		readAndSave(new SavedSignalPath(), params.json, request.apiUser)
	}

	private void readAndSave(SavedSignalPath ssp, Map signalPathMap, SecUser user) {
		Globals globals = GlobalsFactory.createInstance(signalPathMap.settings ?: [:], grailsApplication)

		try {
			// Rebuild the json to check it's ok and up to date
			def signalPathAsMap = signalPathService.reconstruct(signalPathMap, globals)
			def signalPathAsJson = (signalPathAsMap as JSON)

			ssp.name = signalPathAsMap.name
			ssp.json = signalPathAsJson
			ssp.hasExports = signalPathAsMap.hasExports
			ssp.user = user
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

	private SavedSignalPath getAuthorizedSavedSignalPath(long id) {
		def ssp = SavedSignalPath.get(id)
		if (ssp == null) {
			render(status: 404, text: [error: "Canvas with id $id not found.", code: "NOT_FOUND"] as JSON)
		} else if (ssp.type != SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH && !unifinaSecurityService.canAccess(ssp, actionName == 'load', request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Canvas " + id, code: "FORBIDDEN"] as JSON)
		} else {
			return ssp
		}
		return null
	}
}
