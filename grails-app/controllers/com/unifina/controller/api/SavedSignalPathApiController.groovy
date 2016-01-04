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
class SavedSignalPathApiController {

	def signalPathService
	def springSecurityService
	def grailsApplication
	def unifinaSecurityService

	def beforeInterceptor = [
		action: {
			if (params.id != null) {
				def savedSignalPath = SavedSignalPath.get(Integer.parseInt(params.id))
				if (savedSignalPath != null && !unifinaSecurityService.canAccess(savedSignalPath, actionName == 'load', request.apiUser)) {
					if (request.xhr)
						redirect(controller: 'login', action: 'ajaxDenied')
					else
						redirect(controller: 'login', action: 'denied')

					return false
				}
			}
			return true

		},
		only: ['load', 'save']
	]

	private static final Logger log = Logger.getLogger(SavedSignalPathApiController)

	def createSaveData(SavedSignalPath ssp) {
		return [isSaved:true, url:createLink(controller:"savedSignalPathApi",action:"save",params:[id:ssp.id]), name:ssp.name, target: "Archive id "+ssp.id]
	}

	@StreamrApi
	def load() {
		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)

		def ssp = SavedSignalPath.get(Integer.parseInt(params.id))
		if (ssp == null) {
			def errorResult = [error: "Saved signal path with id $params.id not found.", code: "NOT_FOUND"]
			render errorResult as JSON
			return
		}

		Map json = JSON.parse(ssp.json)

		// Reconstruct to bring the path up to date
		json.signalPathData.name = ssp.name

		Map result = json

		try {
			result = signalPathService.reconstruct(json,globals)
		} catch (Throwable e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Error loading SignalPath",e)
			result.error = true
			result.message = message(code:"signalpath.load.error", args:[e.message])
		} finally {
			// Examples can not be saved in place by others than those who have real access to it
			if (ssp.type != SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH || unifinaSecurityService.canAccess(ssp))
				result.saveData = createSaveData(ssp)

			render result as JSON
			globals.destroy()
		}
	}

	@StreamrApi
	def save() {
		SavedSignalPath ssp
		if (params.id)
			ssp = SavedSignalPath.get(params.id)
		else
			ssp = new SavedSignalPath()

		ssp.properties = params

		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)

		try {
			// Make sure the name is set
			Map json = JSON.parse(params.json)
			json.signalPathData.name = params.name
			// Rebuild the json to check it's ok and up to date
			SignalPath sp = signalPathService.jsonToSignalPath(json.signalPathData,true,globals,true)
			json.signalPathData = signalPathService.signalPathToJson(sp)
			ssp.json = (json as JSON)

			ssp.hasExports = sp.hasExports()

			ssp.user = request.apiUser
			ssp.save(flush:true, failOnError:true)

			if (ssp.id==null)
				throw new Exception("Internal error: Returned id was null!")

			def res = createSaveData(ssp)
			render res as JSON
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			log.error("Save failed",e)
			Map r = [error:true, message:message(code:"signalpath.save.error", args:[e.message])]
			render r as JSON
		}
		finally {
			globals.destroy()
		}
	}
}
