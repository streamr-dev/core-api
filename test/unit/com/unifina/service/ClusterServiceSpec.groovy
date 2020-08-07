package com.unifina.service

import com.streamr.api.client.CanvasesPerNode
import com.streamr.api.client.StreamrClient
import com.unifina.domain.security.Key
import com.unifina.domain.security.User
import com.unifina.domain.signalpath.Canvas
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ClusterService)
@Mock([Canvas, User, Key])
class ClusterServiceSpec extends Specification {
	User me
	String apiKey = "token myApiKey"

	def setup() {
		me = new User().save(failOnError: true, validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: false)

		service.streamrClient = Mock(StreamrClient)
		config.streamr.engine.nodes = ["10.0.0.5", "10.0.0.6"]
	}

	void "getCanvases dead"() {
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
		def canvases = service.getCanvases(apiKey)

		then:
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * service.streamrClient._
		canvases.ghost.size() == 0
		canvases.dead.size() == 2
	}

	void "getCanvases ghost"() {
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
		def canvases = service.getCanvases(apiKey)

		then:
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * service.streamrClient._
		canvases.ghost.size() == 2
		canvases.dead.size() == 0
	}

	void "getCanvases dead and ghost"() {
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
		def canvases = service.getCanvases(apiKey)

		then:
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.5") >> canvases1
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.6") >> canvases2
		0 * service.streamrClient._
		canvases.ghost.size() == 2
		canvases.dead.size() == 2
	}

	void "shutdown"() {
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
		def result = service.shutdown(apiKey)

		then:
		1 * service.streamrClient.shutdown(apiKey, "10.0.0.5") >> shutdownResult1
		1 * service.streamrClient.shutdown(apiKey, "10.0.0.6") >> shutdownResult2
		0 * service.streamrClient._
		result.nodes.size() == 2
	}

	void "shutdown noop"() {
		when:
		def results = service.shutdown(apiKey)

		then:
		1 * service.streamrClient.shutdown(apiKey, "10.0.0.5") >> new ArrayList<HashMap<String, Object>>()
		1 * service.streamrClient.shutdown(apiKey, "10.0.0.6") >> new ArrayList<HashMap<String, Object>>()
		0 * service.streamrClient._
		results.nodes.size() == 0
	}

    void "repair and restart dead canvases"() {
		setup:
		service.canvasService = Mock(CanvasService)
		CanvasesPerNode canvasesPerNode = new CanvasesPerNode()
		canvasesPerNode.shouldBeRunning = new ArrayList<HashMap<String, Object>>()
		User u = new User(username: "pena@host.com", password: "abcabcabcabc123", name: "Pena")
		u.id = 1
		u.save(failOnError: true)
		Canvas c = new Canvas(name: "Canvas 1", startedBy: u, json: "{}")
		c.id = "c1"
		c.save(failOnError: true)
		canvasesPerNode.shouldBeRunning.add(new HashMap<String, Object>() {
			{
				put("name",  c.name)
				put("id", c.id)
				put("startedById", u.id)
			}
		})

		when:
		def results = service.repair(apiKey)

		then:
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.5") >> canvasesPerNode
		1 * service.streamrClient.canvasesPerNode(apiKey, "10.0.0.6") >> new CanvasesPerNode()
		0 * service.streamrClient._
		1 * service.canvasService.startRemote(c, u, true, true)
		0 * service.canvasService._
		results.size() == 1
    }
}
