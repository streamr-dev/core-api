package com.unifina.controller.core.signalpath

import com.unifina.domain.signalpath.Canvas
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.atmosphere.cpr.BroadcasterFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.util.FileCopyUtils

import com.unifina.domain.security.SecUser
import com.unifina.service.SignalPathService
import com.unifina.service.UnifinaSecurityService
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

@Secured(["ROLE_USER"])
class CanvasController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	SignalPathService signalPathService
	UnifinaSecurityService unifinaSecurityService
	
	def index() {
		redirect(action: "build", params:params)
	}

	def build() {
		def beginDate = new Date()-1
		def endDate = new Date()-1
		
		def load = null
		
		if (params.load!=null) {
			load = createLink(controller:"canvasApi",action:"load",params:[id:params.load])
		}
		
		[beginDate:beginDate, endDate:endDate, load:load, examples:params.examples, user:SecUser.get(springSecurityService.currentUser.id)]
	}
	
	def reconstruct() {
		Map json = [signalPathContext: (params.signalPathContext ? JSON.parse(params.signalPathContext) : [:]), signalPathData: JSON.parse(params.signalPathData)]
		Globals globals = GlobalsFactory.createInstance(json.signalPathContext, grailsApplication)
		Map result = signalPathService.reconstruct(json, globals)
		render result as JSON
	}
	
	def existsCsv() {
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + params.filename
		File file = new File(fileName)
		Map result = (file.canRead() ? [success:true, filename:params.filename] : [success:false])
		render result as JSON
	}
	
	def downloadCsv() {
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + params.filename
		File file = new File(fileName)
		if (file.canRead()) {
			FileInputStream fileInputStream = new FileInputStream(file)
			response.setContentType("text/csv")
			response.setHeader("Content-disposition", "attachment; filename="+file.name);
			FileCopyUtils.copy(fileInputStream, response.outputStream)
			fileInputStream.close()
			file.delete()
		}
		else throw new FileNotFoundException("File not found: "+params.filename)
	}
	
	@Secured(["ROLE_ADMIN"])
	def debug() {
		return [runners: servletContext["signalPathRunners"], returnChannels: servletContext["returnChannels"], broadcasters: BroadcasterFactory.getDefault().lookupAll()]
	}

	def loadBrowser() {
		def result = [
				browserId: params.browserId,
				headers: ["Id","Name"],
				contentUrl: createLink(
						controller: "canvas",
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
		// TODO: do queries via permissionService once that branch is ready
		if (params.browserId == 'examplesLoadBrowser') {
			ssp = Canvas.executeQuery("select sp.id, sp.name from Canvas sp where sp.type = :type order by sp.dateCreated asc", [type: Canvas.Type.EXAMPLE], [max: max, offset: offset])
		} else if (params.browserId == 'archiveLoadBrowser') {
			ssp = Canvas.executeQuery("select sp.id, sp.name from Canvas sp where sp.user = :user and sp.type = :type order by sp.dateCreated desc", [type: Canvas.Type.TEMPLATE, user:springSecurityService.currentUser], [max: max, offset: offset])
		} else if (params.browserId == 'liveLoadBrowser') {
			ssp = Canvas.executeQuery("select sp.id, sp.name from Canvas sp where sp.user = :user and sp.type = :type order by sp.dateCreated desc", [type: Canvas.Type.RUNNING, user:springSecurityService.currentUser], [max: max, offset: offset])
		}

		def result = [signalPaths:[]]
		ssp.each {
			def tmp = [:]
			tmp.id = it[0]
			tmp.name = it[1]
			tmp.command = params.command
			tmp.offset = offset++
			result.signalPaths.add(tmp)
		}
		return result
	}

	
}
