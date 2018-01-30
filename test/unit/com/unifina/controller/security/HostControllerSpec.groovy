package com.unifina.controller.security

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(HostController)
@Mock([Canvas, SecUser])
class HostControllerSpec extends Specification {

	SecUser user1, user2

	def setup() {
		user1 = new SecUser(username: "user1@streamr.com").save(failOnError: true, validate: false)
		user2 = new SecUser(username: "user2@streamr.com").save(failOnError: true, validate: false)
	}

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
		1 * controller.signalPathService.getUsersOfRunningCanvases() >> ["1": user1, "2": user2]
		1 * controller.signalPathService.stopAllLocalCanvases() >> {
			canvases*.state = Canvas.State.STOPPED
			return canvases
		}
		1 * controller.canvasService.startRemote(canvases[0], user1, false, true)
		1 * controller.canvasService.startRemote(canvases[1], user2, false, true)
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
		1 * controller.signalPathService.getUsersOfRunningCanvases() >> ["1": user1, "2": user2, "3": user1]
		1 * controller.signalPathService.stopAllLocalCanvases() >> {
			canvases*.state = Canvas.State.STOPPED
			return canvases
		}
		1 * controller.canvasService.startRemote(canvases[0], user1, false, true)
		1 * controller.canvasService.startRemote(canvases[1], user2, false, true)
		0 * controller.canvasService.startRemote(canvases[2], _, _, _)
		response.json.size() == 2
	}
	
}
