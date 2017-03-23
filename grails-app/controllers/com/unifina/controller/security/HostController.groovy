package com.unifina.controller.security

import com.unifina.api.ApiException
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_ADMIN"])
class HostController {

	TaskService taskService
	SignalPathService signalPathService
	CanvasService canvasService

	def shutdown() {
		if (request.method.equals("POST")) {
			// Shut down task workers
			taskService.stopAllTaskWorkers()

			// Stop all canvases
			List<Canvas> stoppedCanvases = signalPathService.stopAllLocalCanvases()

			// Discard adhoc canvases
			stoppedCanvases = stoppedCanvases.findAll { !it.adhoc }

			// Create start tasks
			stoppedCanvases.each {
				canvasService.startRemote(it, it.user, false, true)
			}

			render(stoppedCanvases*.toMap() as JSON)
		} else {
			throw new ApiException(405, "METHOD_NOT_ALLOWED", "Dude you need to do a POST")
		}
	}

}
