package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.node.NodeRequest
import com.unifina.api.node.NodeRequestDispatcher
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(NodeApiController)
@Mock([Canvas, SecUser])
class NodeApiControllerSpec extends Specification {

	SecUser user1, user2

	def setup() {
		user1 = new SecUser(username: "user1@streamr.com").save(failOnError: true, validate: false)
		user2 = new SecUser(username: "user2@streamr.com").save(failOnError: true, validate: false)
	}

	void "index lists streamr nodes"() {
		setup:
		config.streamr.nodes = ['192.168.1.51', '192.168.1.53']

		when:
		request.method = "GET"
		controller.index()

		then:
		response.json == ['192.168.1.51', '192.168.1.53']
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

	void "shutdownNode invokes shutdown() if given ipAddress is of current machine"() {
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
		request.method = "POST"
		params.nodeIp = "127.0.0.1"
		controller.shutdownNode()

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

	void "shutdownNode throws ApiException if trying to forward to non-allowed ip"() {
		when:
		request.method = "POST"
		params.nodeIp = "192.168.13.55"
		controller.shutdownNode()

		then:
		def e = thrown(ApiException)
		e.code == "NOT_A_VALID_NODE"
	}

	void "shutdownNode performs shutdown API request on other node if given allowed ip"() {
		setup:
		grailsApplication.config.streamr.nodes = ["192.168.13.55"]
		def nodeRequestDispatcher = controller.nodeRequestDispatcher = Mock(NodeRequestDispatcher)

		when:
		request.method = "POST"
		params.nodeIp = "192.168.13.55"
		controller.shutdownNode()

		then:
		1 * nodeRequestDispatcher.perform({ NodeRequest nodeRequest ->
			assert nodeRequest.url.toString() == "http://192.168.13.55:80/api/v1/nodes/shutdown"
			assert nodeRequest.request.method == "POST"
			return true
		})
	}
	
}
