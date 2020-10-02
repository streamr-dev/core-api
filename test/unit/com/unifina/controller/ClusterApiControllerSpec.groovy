package com.unifina.controller

import com.unifina.domain.Canvas
import com.unifina.domain.Key
import com.unifina.domain.User
import com.unifina.service.ClusterService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.ws.rs.core.HttpHeaders

@TestFor(ClusterApiController)
@Mock([RESTAPIFilters, Canvas, User, Key])
class ClusterApiControllerSpec extends Specification {
	User me
	String apiKey = "token myApiKey"

    def setup() {
		me = new User().save(failOnError: true, validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: false)

		controller.clusterService = Mock(ClusterService)
	}

    void "test dead canvases"() {
		setup:
		ClusterService.Canvases canvases = new ClusterService.Canvases()
		canvases.dead.add(new Canvas(
			id: "c2",
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}",
		))
		canvases.dead.add(new Canvas(
			id: "c5",
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}",
		))

		when:
		request.addHeader(HttpHeaders.AUTHORIZATION, apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.canvases()
		}

		then:
		1 * controller.clusterService.getCanvases(apiKey) >> canvases
		0 * controller.clusterService._
		response.status == 200
		response.json.ghost.size() == 0
		response.json.dead.size() == 2
    }

	void "test ghost canvases"() {
		setup:
		ClusterService.Canvases canvases = new ClusterService.Canvases()
		canvases.ghost.add(new Canvas(
			id: "c2",
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}",
		))
		canvases.ghost.add(new Canvas(
			id: "c5",
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}",
		))

		when:
		request.addHeader(HttpHeaders.AUTHORIZATION, apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.canvases()
		}

		then:
		1 * controller.clusterService.getCanvases(apiKey) >> canvases
		0 * controller.clusterService._
		response.status == 200
		response.json.ghost.size() == 2
		response.json.dead.size() == 0
	}

	void "test dead and ghost canvases"() {
		setup:
		ClusterService.Canvases canvases = new ClusterService.Canvases()
		canvases.dead.add(new Canvas(
			id: "c2",
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}",
		))
		canvases.dead.add(new Canvas(
			id: "c5",
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}",
		))
		canvases.ghost.add(new Canvas(
			id: "c2",
			name: "Canvas #2",
			server: "10.0.0.5",
			state: Canvas.State.RUNNING,
			json: "{}",
		))
		canvases.ghost.add(new Canvas(
			id: "c5",
			name: "Canvas #5",
			server: "10.0.0.6",
			state: Canvas.State.RUNNING,
			json: "{}",
		))

		when:
		request.addHeader(HttpHeaders.AUTHORIZATION, apiKey)
		request.method = "GET"
		withFilters(action: "index") {
			controller.canvases()
		}

		then:
		1 * controller.clusterService.getCanvases(apiKey) >> canvases
		0 * controller.clusterService._
		response.status == 200
		response.json.ghost.size() == 2
		response.json.dead.size() == 2
	}

	void "test shutdown"() {
		setup:
		ClusterService.Nodes result = new ClusterService.Nodes()
		Map<String, Object> result1 = new HashMap<String, Object>()
		result1.put("id", "JRlZpFgdS3-uH4gMh8RgfQxBHz7E__T_uAbj9RxiFkZQ")
		result1.put("name", "Tram Demo")
		result1.put("created", "2018-09-04T13:08:55Z")
		result1.put("updated", "2018-09-04T13:19:57Z")
		result1.put("adhoc", false)
		result1.put("state", "RUNNING")
		result1.put("hasExports", false)
		result1.put("serialized", false)
		result.nodes.add(result1)

		Map<String, Object> result2 = new HashMap<String, Object>()
		result2.put("id", "XXXXXFgdS3-uH4gMh8RgfQxBHz7E__T_uAbj9RxXXXXX")
		result2.put("name", "Tram Demo 2")
		result2.put("created", "2016-04-02T11:08:43Z")
		result2.put("updated", "2016-05-03T11:19:52Z")
		result2.put("adhoc", false)
		result2.put("state", "RUNNING")
		result2.put("hasExports", false)
		result2.put("serialized", false)
		result.nodes.add(result2)

		when:
		request.addHeader(HttpHeaders.AUTHORIZATION, apiKey)
		request.method = "POST"
		withFilters(action: "shutdown") {
			controller.shutdown()
		}

		then:
		1 * controller.clusterService.shutdown(apiKey) >> result
		0 * controller.clusterService._
		response.status == 200
		response.json.nodeResults.size() == 2
	}

	void "test shutdown noop"() {
		when:
		request.addHeader(HttpHeaders.AUTHORIZATION, apiKey)
		request.method = "POST"
		withFilters(action: "shutdown") {
			controller.shutdown()
		}

		then:
		1 * controller.clusterService.shutdown(apiKey) >> new ClusterService.Nodes()
		0 * controller.clusterService._
		response.status == 200
		response.json.nodeResults.size() == 0
	}

	void "test repair"() {
		when:
		request.addHeader(HttpHeaders.AUTHORIZATION, apiKey)
		request.method = "POST"
		withFilters(action: "repair") {
			controller.repair()
		}

		then:
		1 * controller.clusterService.repair(apiKey) >> new ArrayList<Canvas>()
		0 * controller.clusterService._
		response.status == 200
		response.json.restartedNodes.size() == 0
	}
}
