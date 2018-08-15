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
}
