package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class NodeApiController {

	static allowedMethods = [
		shutdown: "POST"
	]

	GrailsApplication grailsApplication
	CanvasService canvasService
	SignalPathService signalPathService
	TaskService taskService


	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def index() {
		render(getStreamrNodes() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def shutdown() {
		// Shut down task workers
		taskService.stopAllTaskWorkers()

		// Get users of running canvases
		Map<String, SecUser> canvasIdToUser = signalPathService.getUsersOfRunningCanvases()

		// Stop all canvases
		List<Canvas> stoppedCanvases = signalPathService.stopAllLocalCanvases()

		// Discard adhoc canvases
		stoppedCanvases = stoppedCanvases.findAll { !it.adhoc }

		// Create start tasks
		stoppedCanvases.each {
			canvasService.startRemote(it, canvasIdToUser[it.id], false, true)
		}

		render(stoppedCanvases*.toMap() as JSON)
	}

	private List<String> getStreamrNodes() {
		(List<String>) grailsApplication.config.streamr.nodes
	}
}
