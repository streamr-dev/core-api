package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.node.NodeRequest
import com.unifina.api.node.NodeRequestDispatcher
import com.unifina.domain.User
import com.unifina.domain.Canvas
import com.unifina.service.CanvasService
import com.unifina.service.NodeService
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import com.unifina.signalpath.SignalPath
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(NodeApiController)
@Mock([Canvas, User])
class NodeApiControllerSpec extends Specification {

	User user1, user2

	def setup() {
		user1 = new User(username: "user1@streamr.com").save(failOnError: true, validate: false)
		user2 = new User(username: "user2@streamr.com").save(failOnError: true, validate: false)

		controller.nodeService = Mock(NodeService)
	}

	void "index lists streamr nodes"() {
		setup:
		config.streamr.engine.nodes = ['192.168.1.51', '192.168.1.53']

		when:
		request.method = "GET"
		controller.index()

		then:
		response.json == ['192.168.1.51', '192.168.1.53']
	}

	void "ip() returns node IP address"() {
		when:
		request.method = "GET"
		controller.ip()

		then:
		1 * controller.nodeService.getIPAddress() >> "1.2.3.4"
		response.json["ip"] == "1.2.3.4"
	}

	void "config returns the Grails config as flattened json"() {
		when:
		request.method = "GET"
		controller.config()

		then:
		response.json.size() > 0
		response.json["streamr.engine.nodes"] != null
	}

	void "shutdown must stop all TaskWorkers, stop local Canvases and start them remotely"() {
		def canvases = [
				new Canvas(state: Canvas.State.RUNNING),
				new Canvas(state: Canvas.State.RUNNING)
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
				new Canvas(state: Canvas.State.RUNNING),
				new Canvas(state: Canvas.State.RUNNING),
				new Canvas(state: Canvas.State.RUNNING, adhoc: true)
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

	void "canvases lists empty canvases if nothing running and nothing marked as running in DB"() {
		setup:
		controller.signalPathService = Stub(SignalPathService) {
			getRunningSignalPaths() >> ([] as Set<SignalPath>)
		}

		when:
		request.method = "GET"
		controller.canvases()

		then:
		response.status == 200
		response.json == [
			ok: [],
		    shouldBeRunning: [],
			shouldNotBeRunning: []
		]
	}

	void "canvases lists canvases according to whether they are really running and according to DB"() {
		setup: "setup Canvases"
		def canvases = [
			new Canvas(name: "Canvas #1", state: Canvas.State.RUNNING, server: "10.0.0.5"),
			new Canvas(name: "Canvas #2", state: Canvas.State.RUNNING, server: "10.0.0.5"),
			new Canvas(name: "Canvas #3", state: Canvas.State.RUNNING, server: "10.0.0.5"),
			new Canvas(name: "Canvas #4", state: Canvas.State.STOPPED, server: "10.0.0.5"),
			new Canvas(name: "Canvas #5", state: Canvas.State.STOPPED, server: "10.0.0.5"),
			new Canvas(name: "Canvas #6", state: Canvas.State.RUNNING, server: "10.0.0.6"),
		]
		canvases.eachWithIndex { Canvas c, int index -> c.id = "canvas-${index+1}" }
		canvases*.save(failOnError: true, validate: false)

		def unsavedCanvas = new Canvas(name: "unsaved canvas")
		unsavedCanvas.id = "non-existing-canvas-id"

		and: "setup SignalPaths"
		def runningCanvas1 = new SignalPath()
		def runningCanvas2 = new SignalPath()
		def runningCanvas3 = new SignalPath()
		def runningCanvas4 = new SignalPath()
		runningCanvas1.canvas = canvases[0]
		runningCanvas2.canvas = canvases[1]
		runningCanvas3.canvas = canvases[3]
		runningCanvas4.canvas = unsavedCanvas

		controller.signalPathService = Stub(SignalPathService) {
			getRunningSignalPaths() >> ([
				runningCanvas1,
				runningCanvas2,
				runningCanvas3,
				runningCanvas4
			] as Set<SignalPath>)
		}

		when:
		request.method = "GET"
		controller.canvases()

		then:
		1 * controller.nodeService.getIPAddress() >> "10.0.0.5"

		and:
		response.status == 200
		response.json.keySet() == ["ok", "shouldBeRunning", "shouldNotBeRunning"] as Set
		response.json.ok*.id == ["canvas-1", "canvas-2"]
		response.json.shouldBeRunning*.id == ["canvas-3"]
		response.json.shouldNotBeRunning*.id == ["canvas-4", "non-existing-canvas-id"]
	}

	void "canvasSizes returns empty map if nothing running"() {
		setup:
		controller.signalPathService = Stub(SignalPathService) {
			getRunningSignalPaths() >> ([] as Set)
		}

		when:
		request.method = "GET"
		controller.canvasSizes()

		then:
		response.status == 200
		response.json == [:]
	}

	void "canvasSizes uses serializationService#serialize to determine canvas size"() {
		setup: "setup Canvases"
		def canvases = [
			new Canvas(name: "Canvas #1", state: Canvas.State.RUNNING, server: "10.0.0.5"),
			new Canvas(name: "Canvas #2", state: Canvas.State.RUNNING, server: "10.0.0.5")
		]
		canvases.eachWithIndex { Canvas c, int index -> c.id = "canvas-${index+1}" }
		canvases*.save(failOnError: true, validate: false)

		and: "setup SignalPaths"
		def runningCanvas1 = new SignalPath()
		def runningCanvas2 = new SignalPath()
		runningCanvas1.canvas = canvases[0]
		runningCanvas2.canvas = canvases[1]

		controller.signalPathService = Stub(SignalPathService) {
			getRunningSignalPaths() >> ([
				runningCanvas1,
				runningCanvas2,
			] as Set<SignalPath>)
		}

		def serializationService = controller.serializationService = Mock(SerializationService)

		when:
		request.method = "GET"
		controller.canvasSizes()

		then:
		1 * serializationService.serialize(runningCanvas1) >> new byte[256]
		1 * serializationService.serialize(runningCanvas2) >> new byte[666]
		0 * serializationService._

		and:
		response.status == 200
		response.json == [
		    "canvas-1": 256,
			"canvas-2": 666
		]
	}

	void "shutdownNode invokes shutdown() if given ipAddress is of current machine"() {
		def canvases = [
			new Canvas(state: Canvas.State.RUNNING,),
			new Canvas(state: Canvas.State.RUNNING,),
			new Canvas(state: Canvas.State.RUNNING, adhoc: true)
		]
		canvases*.save(validate:false)

		controller.taskService = Mock(TaskService)
		controller.signalPathService = Mock(SignalPathService)
		controller.canvasService = Mock(CanvasService)

		when:
		request.method = "POST"
		params.nodeIp = "1.2.3.4"
		controller.shutdownNode()

		then:
		1 * controller.nodeService.isIpAddressOfCurrentNode("1.2.3.4") >> true
		1 * controller.signalPathService.getUsersOfRunningCanvases() >> ["1": user1, "2": user2, "3": user1]
		1 * controller.signalPathService.stopAllLocalCanvases() >> {
			canvases*.state = Canvas.State.STOPPED
			return canvases
		}
		1 * controller.canvasService.startRemote(canvases[0], user1, false, true)
		1 * controller.canvasService.startRemote(canvases[1], user2, false, true)
		0 * controller.canvasService.startRemote(canvases[2], _, _, _)

		and:
		response.status == 200
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
		grailsApplication.config.streamr.engine.nodes = ["192.168.13.55"]
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

	void "canvasesNode invokes canvases() if given ipAddress is of current machine"() {
		controller.signalPathService = Stub(SignalPathService)

		when:
		request.method = "GET"
		params.nodeIp = "1.2.3.4"
		controller.canvasesNode()

		then:
		1 * controller.nodeService.isIpAddressOfCurrentNode("1.2.3.4") >> true
		response.status == 200
		response.json.keySet() == ["ok", "shouldBeRunning", "shouldNotBeRunning"] as Set
	}

	void "canvasesNode throws ApiException if trying to forward to non-allowed ip"() {
		when:
		request.method = "GET"
		params.nodeIp = "192.168.13.57"
		controller.canvasesNode()

		then:
		def e = thrown(ApiException)
		e.code == "NOT_A_VALID_NODE"
	}

	void "canvasesNode performs shutdown API request on other node if given allowed ip"() {
		setup:
		grailsApplication.config.streamr.engine.nodes = ["192.168.13.55"]
		def nodeRequestDispatcher = controller.nodeRequestDispatcher = Mock(NodeRequestDispatcher)

		when:
		request.method = "GET"
		params.nodeIp = "192.168.13.55"
		controller.canvasesNode()

		then:
		1 * nodeRequestDispatcher.perform({ NodeRequest nodeRequest ->
			assert nodeRequest.url.toString() == "http://192.168.13.55:80/api/v1/nodes/canvases"
			assert nodeRequest.request.method == "GET"
			return true
		})
	}

}
