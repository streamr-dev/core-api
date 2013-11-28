package com.unifina.signalpath

import grails.converters.JSON
import grails.util.GrailsUtil

import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

class SavedSignalPathController {
	
	def signalPathService
	def springSecurityService
	def grailsApplication
	
	def unifinaSecurityService
	def beforeInterceptor = [action:{unifinaSecurityService.canAccess(SavedSignalPath.get(params.id))},only:['load', 'save']]
	
	def createSaveData(SavedSignalPath ssp) {
		return [url:createLink(controller:"savedSignalPath",action:"save",params:[id:ssp.id]), name:ssp.name, target: "Archive id "+ssp.id]
	}
	
	def load() {
		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)
		
		try {
			def ssp = SavedSignalPath.get(Integer.parseInt(params.id))		
			Map json = JSON.parse(ssp.json);
	
			// Reconstruct to bring the path up to date
			json.signalPathData.name = ssp.name
			Map result = signalPathService.reconstruct(json,globals)
			
			result.saveData = createSaveData(ssp) 

			render result as JSON
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			Map r = [error:true, message:e.message]
			render r as JSON
		} finally {
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
			ssp.save()

			if (ssp.id==null)
				throw new Exception("Internal error: Returned id was null!")
			
			def res = createSaveData(ssp)
			render res as JSON
		} catch (Exception e) {
			e = GrailsUtil.deepSanitize(e)
			Map r = [error:true, message:"SIGNALPATH NOT SAVED:\n"+e.message]
			render r as JSON
		}
		finally {
			globals.destroy()
		}
	}
	
	def loadBrowser() {
		def result = [browserId:params.browserId, headers:["Id","Name"], contentUrl:createLink(controller:"savedSignalPath",action:"loadBrowserContent",params:[browserId:params.browserId,command:params.command])]
		render(template:"loadBrowser",model:result)
	}
	
	def loadBrowserContent() {
		def max = params.int("max") ?: 100
		def offset = params.int("offset") ?: 0
		
		def ssp = SavedSignalPath.executeQuery("select sp.id, sp.name from SavedSignalPath sp where sp.user = :user order by sp.id desc", [user:springSecurityService.currentUser], [max: max, offset: offset])
		def result = [signalPaths:[]]
		ssp.each {
			def tmp = [:]
			tmp.id = it[0]
			tmp.name = it[1]
			tmp.url = createLink(controller:"savedSignalPath",action:"load",params:[id:it[0]])
			tmp.command = params.command
			result.signalPaths.add(tmp)
		}
		return result
	}

	
}
