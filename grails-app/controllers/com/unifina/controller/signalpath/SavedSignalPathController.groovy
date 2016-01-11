package com.unifina.controller.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.GrailsUtil

import org.apache.log4j.Logger

import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

@Secured(["ROLE_USER"])
class SavedSignalPathController {
	
	def signalPathService
	def springSecurityService
	def grailsApplication
	
	def unifinaSecurityService
	def beforeInterceptor = [action:{
			if (params.id!=null && !unifinaSecurityService.canAccess(SavedSignalPath.get(params.id), actionName=='load')) {
				if (request.xhr)
					redirect(controller:'login', action:'ajaxDenied')
				else
					redirect(controller:'login', action:'denied')
					
				return false
			}
			else return true
		},only:['load', 'save']]
	
	private static final Logger log = Logger.getLogger(SavedSignalPathController)
	
	def createSaveData(SavedSignalPath ssp) {
		return [isSaved:true, url:createLink(controller:"canvasesApi",action:"save",params:[id:ssp.id]), name:ssp.name, target: "Archive id "+ssp.id]
	}
	
	def load() {
		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)
		
		def ssp = SavedSignalPath.get(Integer.parseInt(params.id))
		Map json = JSON.parse(ssp.json);

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

//			ssp.live = json.signalPathContext.live
			ssp.user = springSecurityService.currentUser
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
	
	def loadBrowser() {
		def result = [
			browserId: params.browserId,
			headers: ["Id","Name"],
			contentUrl: createLink(
				controller: "savedSignalPath",
				action: "loadBrowserContent",
				params: [
					browserId: params.browserId,
					command: params.command
				]
			)
		]
		
		render(template: "loadBrowser", model: result)
	}
	
	def loadBrowserContent() {
		def max = params.int("max") ?: 100
		def offset = params.int("offset") ?: 0
		def ssp 
		if (params.browserId == 'examplesLoadBrowser') {
			ssp = SavedSignalPath.executeQuery("select sp.id, sp.name from SavedSignalPath sp where sp.type = :type order by sp.id asc", [type:SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH], [max: max, offset: offset])
		} else {
			ssp = SavedSignalPath.executeQuery("select sp.id, sp.name from SavedSignalPath sp where sp.user = :user order by sp.id desc", [user:springSecurityService.currentUser], [max: max, offset: offset])
		}
		
		def result = [signalPaths:[]]
		ssp.each {
			def tmp = [:]
			tmp.id = it[0]
			tmp.name = it[1]
			tmp.url = createLink(controller:"canvasesApi",action:"load",params:[id:it[0]])
			tmp.command = params.command
			tmp.offset = offset++
			result.signalPaths.add(tmp)
		}
		return result
	}

	
}
