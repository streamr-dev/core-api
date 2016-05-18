package com.unifina.controller.security

import com.unifina.api.ApiException
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(HostController)
@Mock(Canvas)
class HostControllerSpec extends Specification {

	void "shutdown throws ApiException for wrong http verb"() {
		when:
		request.method = 'GET'
		controller.shutdown()

		then:
		ApiException e = thrown(ApiException)
		e.statusCode == 405
	}

	void "shutdown must stop all TaskWorkers, stop local Canvases and start them remotely"() {
		def canvases = [
				new Canvas(state: Canvas.State.RUNNING, json: "{}"),
				new Canvas(state: Canvas.State.RUNNING, json: "{}")
		]
		canvases*.save(validate:false)

		controller.taskService = Mock(TaskService)
		controller.signalPathService = Mock(SignalPathService)
		controller.canvasService = Mock(CanvasService)

		when:
		request.method = 'POST'
		controller.shutdown()

		then:
		1 * controller.taskService.stopAllTaskWorkers()
		1 * controller.signalPathService.stopAllLocalCanvases() >> {
			canvases*.state = Canvas.State.STOPPED
			return canvases
		}
		1 * controller.canvasService.startRemote(canvases[0], false, true)
		1 * controller.canvasService.startRemote(canvases[1], false, true)
		response.json.size() == 2
		response.json[0].id == "1"
		response.json[1].id == "2"
	}

	void "shutdown must not create start tasks for adhoc canvases"() {
		def canvases = [
				new Canvas(state: Canvas.State.RUNNING, json: "{}"),
				new Canvas(state: Canvas.State.RUNNING, json: "{}"),
				new Canvas(state: Canvas.State.RUNNING, json: "{}", adhoc: true)
		]
		canvases*.save(validate:false)

		controller.taskService = Mock(TaskService)
		controller.signalPathService = Mock(SignalPathService)
		controller.canvasService = Mock(CanvasService)

		when:
		request.method = 'POST'
		controller.shutdown()

		then:
		1 * controller.signalPathService.stopAllLocalCanvases() >> {
			canvases*.state = Canvas.State.STOPPED
			return canvases
		}
		1 * controller.canvasService.startRemote(canvases[0], false, true)
		1 * controller.canvasService.startRemote(canvases[1], false, true)
		0 * controller.canvasService.startRemote(canvases[2], false, true)
		response.json.size() == 2
	}
	
}
