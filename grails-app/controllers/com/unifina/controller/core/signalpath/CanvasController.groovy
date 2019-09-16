package com.unifina.controller.core.signalpath

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import com.unifina.service.SignalPathService
import com.unifina.utils.Globals

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

import java.security.AccessControlException

@Secured(["ROLE_USER"])
class CanvasController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	SpringSecurityService springSecurityService
	SignalPathService signalPathService
	PermissionService permissionService

	def index() {
		redirect(action: "editor", params:params)
	}

	def list() {
		def user = springSecurityService.currentUser
		Closure criteriaFilter = {
			eq "adhoc", false
			if (params.term) {
				like "name", "%${params.term}%"
			}
			if (params.state) {
				inList "state", params.list("state").collect { String param -> Canvas.State.fromValue(param) }
			}
			order "lastUpdated", "desc"
		}
		List<Canvas> readableCanvases = permissionService.get(Canvas, user, Operation.READ, criteriaFilter)
		Set<Canvas> shareableCanvases = permissionService.get(Canvas, user, Operation.SHARE, criteriaFilter).toSet()
		Set<Canvas> writableCanvases = permissionService.get(Canvas, user, Operation.WRITE, criteriaFilter).toSet()
		[canvases: readableCanvases, shareableCanvases: shareableCanvases, writableCanvases: writableCanvases, user: user, stateFilter: params.state ? params.list("state") : []]
	}

	def editor() {
		def beginDate = new Date()
		def endDate = new Date()
		def currentUser = SecUser.get(springSecurityService.currentUser.id)
		def addStream
		def error

		if (params.addStream) {
			Stream stream = Stream.findById(params.addStream)
			if (!permissionService.canRead(currentUser, stream)) {
				error = new AccessControlException("Cannot add stream - User ${currentUser.getUsername()} does not have read access to Stream with id ${params.addStream}")
			} else {
				addStream = params.addStream
			}
		}

		[
			beginDate: beginDate,
			endDate: endDate,
			id: params.id,
			examples: params.examples,
			user: currentUser,
			key: currentUser?.keys?.iterator()?.next(), // any one of the user's keys will do
			addModuleId: params.addModule,
			addStreamId: addStream,
			error: error,
		]
	}

	// Can be accessed anonymously for embedding canvases in iframes (eg. the landing page)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def embed() {
		[id:params.id]
	}

	def reconstruct() {
		Map json = [signalPathContext: (params.signalPathContext ? JSON.parse(params.signalPathContext) : [:]), signalPathData: JSON.parse(params.signalPathData)]
		Globals globals = new Globals(json.signalPathContext, null)
		SignalPathService.ReconstructedResult result = signalPathService.reconstruct(json, globals)
		render result.map as JSON
	}

	def existsCsv() {
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + params.filename
		File file = new File(fileName)
		Map result = (file.canRead() ? [success:true, filename:params.filename] : [success:false])
		render result as JSON
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

		def canvases = []
		if (params.browserId == 'examplesLoadBrowser') {
			// Example canvases functionality removed. Always return an empty list.
			canvases = []
		} else if (params.browserId == 'archiveLoadBrowser') {
			def user = springSecurityService.currentUser
			canvases = permissionService.get(Canvas, user, Operation.READ) {
				eq "adhoc", false
				order "lastUpdated", "desc"
				maxResults max
				firstResult offset
			}
		}

		return [
			canvases: canvases.collect {[
				id: it.id,
				name: it.name,
				state: it.state,
				command: params.command,
				offset: offset++
			]}
		]
	}


}
