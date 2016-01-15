package com.unifina.controller.signalpath

import com.unifina.domain.signalpath.Canvas
import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_USER"])
class SavedSignalPathController {

	def springSecurityService
	def grailsApplication
	def unifinaSecurityService
	
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

	// TODO: refactor to Canvas fully
	def loadBrowserContent() {
		def max = params.int("max") ?: 100
		def offset = params.int("offset") ?: 0
		def ssp 
		if (params.browserId == 'examplesLoadBrowser') {
			ssp = Canvas.executeQuery("select sp.id, sp.name from Canvas sp where sp.type = :type order by sp.id asc", [type: Canvas.Type.EXAMPLE.id], [max: max, offset: offset])
		} else {
			ssp = Canvas.executeQuery("select sp.id, sp.name from Canvas sp where sp.user = :user order by sp.id desc", [user:springSecurityService.currentUser], [max: max, offset: offset])
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
