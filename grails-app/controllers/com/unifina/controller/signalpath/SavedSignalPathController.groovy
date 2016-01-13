package com.unifina.controller.signalpath

import com.unifina.domain.signalpath.SavedSignalPath
import grails.plugin.springsecurity.annotation.Secured
import org.apache.log4j.Logger

@Secured(["ROLE_USER"])
class SavedSignalPathController {
	
	def signalPathService
	def springSecurityService
	def grailsApplication
	
	def unifinaSecurityService
	
	private static final Logger log = Logger.getLogger(SavedSignalPathController)
	
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
			tmp.url = createLink(controller:"canvasApi",action:"load",params:[id:it[0]])
			tmp.command = params.command
			tmp.offset = offset++
			result.signalPaths.add(tmp)
		}
		return result
	}

	
}
