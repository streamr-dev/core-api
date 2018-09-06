package com.unifina.controller.api

import com.streamr.api.client.CanvasesPerNode
import com.streamr.api.client.StreamrClient
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.filters.UnifinaCoreAPIFilters
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ClusterApiController)
@Mock([UnifinaCoreAPIFilters, Canvas, SecUser, Key])
class ClusterApiControllerSpec extends Specification {
	SecUser me
	String apiKey = "token myApiKey"

    def setup() {
		me = new SecUser().save(failOnError: true, validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: false)

		controller.client = Mock(StreamrClient)
		config.streamr.nodes = ["10.0.0.5", "10.0.0.6"]
	}

    void "test dead canvases"() {
		setup:
		CanvasesPerNode canvases1  = new CanvasesPerNode()
		canvases1.shouldBeRunning = new Canvas[1]
		canvases1.shouldBeRunning[0] = new Canvas(
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases1.shouldBeRunning[0].id = "c2"
		canvases1.shouldBeRunning[0].save(failOnError: true, validate: false)

		CanvasesPerNode canvases2  = new CanvasesPerNode()
		canvases2.shouldBeRunning = new Canvas[1]
		canvases2.shouldBeRunning[0] = new Canvas(
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases2.shouldBeRunning[0].id = "c5"
		canvases2.shouldBeRunning[0].save(failOnError: true, validate: false)

		when:
		request.addHeader("Authorization", apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * controller.client._
		response.status == 200
		response.json.ghost.size() == 0
		response.json.dead.size() == 2
    }

	void "test ghost canvases"() {
		setup:
		CanvasesPerNode canvases1  = new CanvasesPerNode()

		canvases1.shouldNotBeRunning = new Canvas[1]
		canvases1.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases1.shouldNotBeRunning[0].id = "c2"
		canvases1.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		CanvasesPerNode canvases2  = new CanvasesPerNode()

		canvases2.shouldNotBeRunning = new Canvas[1]
		canvases2.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases2.shouldNotBeRunning[0].id = "c5"
		canvases2.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		when:
		request.addHeader("Authorization", apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * controller.client._
		response.status == 200
		response.json.ghost.size() == 2
		response.json.dead.size() == 0
	}

	void "test dead and ghost canvases"() {
		setup:
		CanvasesPerNode canvases1  = new CanvasesPerNode()
		canvases1.shouldBeRunning = new Canvas[1]
		canvases1.shouldBeRunning[0] = new Canvas(
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases1.shouldBeRunning[0].id = "c2"
		canvases1.shouldBeRunning[0].save(failOnError: true, validate: false)

		canvases1.shouldNotBeRunning = new Canvas[1]
		canvases1.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases1.shouldNotBeRunning[0].id = "c2"
		canvases1.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		CanvasesPerNode canvases2  = new CanvasesPerNode()
		canvases2.shouldBeRunning = new Canvas[1]
		canvases2.shouldBeRunning[0] = new Canvas(
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases2.shouldBeRunning[0].id = "c5"
		canvases2.shouldBeRunning[0].save(failOnError: true, validate: false)

		canvases2.shouldNotBeRunning = new Canvas[1]
		canvases2.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases2.shouldNotBeRunning[0].id = "c5"
		canvases2.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		when:
		request.addHeader("Authorization", apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * controller.client._
		response.status == 200
		response.json.ghost.size() == 2
		response.json.dead.size() == 2
	}
/*
	void "test canvases running on wrong machine"() {
		setup:
		CanvasesPerNode canvases1  = new CanvasesPerNode()

		canvases1.shouldNotBeRunning = new Canvas[1]
		canvases1.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases1.shouldNotBeRunning[0].id = "c2"
		canvases1.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		CanvasesPerNode canvases2  = new CanvasesPerNode()

		canvases2.shouldNotBeRunning = new Canvas[1]
		canvases2.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases2.shouldNotBeRunning[0].id = "c5"
		canvases2.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		when:
		request.addHeader("Authorization", apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * controller.client._
		response.status == 200
		response.json.ghost.size() == 0
		response.json.dead.size() == 0
		response.json.wrongMachine.size() == 2
		response.json.multipleMachines.size() == 0
	}

	void "test canvases running on multiple machines"() {
		setup:
		CanvasesPerNode canvases1  = new CanvasesPerNode()

		canvases1.shouldNotBeRunning = new Canvas[1]
		canvases1.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases1.shouldNotBeRunning[0].id = "c2"
		canvases1.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		CanvasesPerNode canvases2  = new CanvasesPerNode()

		canvases2.shouldNotBeRunning = new Canvas[1]
		canvases2.shouldNotBeRunning[0] = new Canvas(
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}"
		)
		canvases2.shouldNotBeRunning[0].id = "c5"
		canvases2.shouldNotBeRunning[0].save(failOnError: true, validate: false)

		when:
		request.addHeader("Authorization", apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * controller.client.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * controller.client._
		response.status == 200
		response.json.ghost.size() == 0
		response.json.dead.size() == 0
		response.json.wrongMachine.size() == 0
		response.json.multipleMachines.size() == 2
	}
*/

	void "test shutdown"() {
		setup:
		List<Map<String, Object>> shutdownResult1 = new ArrayList<HashMap<String, Object>>()
		Map<String, Object> result1 = new HashMap<String, Object>()
		result1.put("id", "JRlZpFgdS3-uH4gMh8RgfQxBHz7E__T_uAbj9RxiFkZQ")
		result1.put("name", "Tram Demo")
		result1.put("created", "2018-09-04T13:08:55Z")
		result1.put("updated", "2018-09-04T13:19:57Z")
		result1.put("adhoc", false)
		result1.put("state", "RUNNING")
		result1.put("hasExports", false)
		result1.put("serialized", false)
		shutdownResult1.add(result1)

		List<Map<String, Object>> shutdownResult2 = new ArrayList<HashMap<String, Object>>()
		Map<String, Object> result2 = new HashMap<String, Object>()
		result2.put("id", "XXXXXFgdS3-uH4gMh8RgfQxBHz7E__T_uAbj9RxXXXXX")
		result2.put("name", "Tram Demo 2")
		result2.put("created", "2018-09-04T13:08:55Z")
		result2.put("updated", "2018-09-04T13:19:57Z")
		result2.put("adhoc", false)
		result2.put("state", "RUNNING")
		result2.put("hasExports", false)
		result2.put("serialized", false)
		shutdownResult2.add(result1)

		when:
		request.addHeader("Authorization", apiKey)
		request.method = "POST"
		withFilters(action: "shutdown") {
			controller.shutdown()
		}

		then:
		1 * controller.client.shutdown(apiKey, "10.0.0.5") >> shutdownResult1
		1 * controller.client.shutdown(apiKey, "10.0.0.6") >> shutdownResult2
		0 * controller.client._
		response.status == 200
		response.json.nodeResults.size() == 2
	}

	void "test shutdown noop"() {
		when:
		request.addHeader("Authorization", apiKey)
		request.method = "POST"
		withFilters(action: "shutdown") {
			controller.shutdown()
		}

		then:
		1 * controller.client.shutdown(apiKey, "10.0.0.5") >> new ArrayList<HashMap<String, Object>>()
		1 * controller.client.shutdown(apiKey, "10.0.0.6") >> new ArrayList<HashMap<String, Object>>()
		0 * controller.client._
		response.status == 200
		response.json.nodeResults.size() == 0
	}
}
