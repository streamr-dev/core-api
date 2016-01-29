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
import com.unifina.service.PermissionService
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

@Secured(["ROLE_USER"])
class CanvasController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	SignalPathService signalPathService
	PermissionService permissionService
	
	def index() {
		redirect(action: "editor", params:params)
	}

	def list() {
		// TODO: replace query with permissionService method once that branch is ready
		List<Canvas> canvases = Canvas.createCriteria().list() {
			eq("user",springSecurityService.currentUser)
			eq("adhoc",false)
			if (params.term) {
				like("name","%${params.term}%")
			}
			if (params.state) {
				inList("state", params.list("state").collect {Canvas.State.valueOf(it.toUpperCase())})
			}
		}
		[canvases: canvases, user:springSecurityService.currentUser, stateFilter: params.state ? params.list("state") : []]
	}

	def editor() {
		def beginDate = new Date()-1
		def endDate = new Date()-1

		[beginDate:beginDate, endDate:endDate, id:params.id, examples:params.examples, user:SecUser.get(springSecurityService.currentUser.id)]
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
				headers: ["Name", "State"],
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
			ssp = Canvas.executeQuery("select sp.id, sp.name, sp.state from Canvas sp where sp.example = true order by sp.dateCreated asc", [max: max, offset: offset])
		} else if (params.browserId == 'archiveLoadBrowser') {
			ssp = Canvas.executeQuery("select sp.id, sp.name, sp.state from Canvas sp where sp.example = false and sp.user = :user and sp.adhoc = false order by sp.dateCreated desc", [user:springSecurityService.currentUser], [max: max, offset: offset])
		}

		def result = [signalPaths:[]]
		ssp.each {
			def tmp = [:]
			tmp.id = it[0]
			tmp.name = it[1]
			tmp.state = it[2]
			tmp.command = params.command
			tmp.offset = offset++
			result.signalPaths.add(tmp)
		}
		return result
	}


}
