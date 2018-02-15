package com.unifina.controller.api

import com.unifina.api.CanvasListParams
import com.unifina.api.ListParams
import com.unifina.api.NotPermittedException
import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.service.UserService
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.Specification

@TestFor(CanvasApiController)
@Mock([SecUser, Permission, Canvas, Key, UnifinaCoreAPIFilters])
class CanvasApiControllerSpec extends Specification {

	ApiService apiService
	CanvasService canvasService
	SecUser me
	Canvas canvas1
	Canvas canvas2
	Canvas canvas3

	// This gets the real services injected into the filters
	// From https://github.com/grails/grails-core/issues/9191
	static doWithSpring = {
		springSecurityService(SpringSecurityService)
		userService(UserService)
	}

	void setup() {
		controller.canvasService = canvasService = Mock(CanvasService)
		controller.signalPathService = Mock(SignalPathService)
		controller.apiService = apiService = Mock(ApiService)

		me = new SecUser(id: 1).save(validate: false)
		SecUser other = new SecUser(id: 2).save(validate: false)

		def k1 = new Key(name: "k1", user: me)
		k1.id = "myApiKey"
		k1.save(failOnError: true, validate: true)

		def k2 = new Key(name: "k2", user: other)
		k2.id = "otherApiKey"
		k2.save(failOnError: true, validate: true)

		canvas1 = new Canvas(
			name: "mine",
			json: new JsonBuilder([name: "mine", modules: [[hash: 1]], settings: [:]]).toString(),
			state: Canvas.State.STOPPED,
			hasExports: false
		)
		canvas1.save(validate: true, failOnError: true)

		canvas2 = new Canvas(
			name: "not mine",
			json: '{name: "not mine", modules: []}',
			state: Canvas.State.STOPPED,
			hasExports: false
		).save(validate: true, failOnError: true)

		canvas3 = new Canvas(
			name: "not mine but example",
			json: '{name: "not mine but example", modules: []}',
			state: Canvas.State.STOPPED,
			example: true,
			hasExports: false
		).save(validate: true, failOnError: true)

		assert SecUser.count() == 2
		assert Canvas.count() == 3
	}

	void "index() renders authorized canvases as a list"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 3
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams().toMap()
			[canvas1, canvas2, canvas3]
		}
	}

	void "index() adds name param to filter criteria"() {
		when:
		params.name = "Foo"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams(name: "Foo").toMap()
			[]
		}
	}

	void "index() adds adhoc param to filter criteria"() {
		when:
		params.adhoc = "true"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams(adhoc: true).toMap()
			[]
		}
	}

	void "index() adds state param to filter criteria"() {
		when:
		params.state = "RUNNING"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams(state: Canvas.State.RUNNING).toMap()
			[]
		}
	}

	void "show() authorizes, reconstructs and renders the canvas as json"() {
		when:
		params.id = "1"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/$params.id"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json?.size() > 0

		1 * canvasService.authorizedGetById("1", me, Permission.Operation.READ) >> canvas1
		1 * canvasService.reconstruct(canvas1, me) >> { Canvas c, SecUser user -> JSON.parse(c.json) }
	}

	void "show() supports runtime parameter"() {
		when:
		params.id = canvas1.id
		params.runtime = true
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/$params.id"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json?.size() > 0

		1 * canvasService.authorizedGetById("1", me, Permission.Operation.READ) >> canvas1
		1 * controller.signalPathService.runtimeRequest(_, false) >> [success:true, json:JSON.parse(canvas1.json)]
	}

	void "save() creates a new canvas and renders it as json"() {
		def newCanvasId

		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.JSON = [
			name: "brand new Canvas",
			modules: [],
		]
		request.method = "POST"
		request.requestURI = "/api/v1/canvases"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 200
		response.json.id == newCanvasId
		1 * canvasService.createNew(_, me) >> { SaveCanvasCommand command, SecUser user ->
			assert command.name == "brand new Canvas"
			assert command.modules == []
			def c = new Canvas(json: "{}").save(validate: false)
			newCanvasId = c.id
			return c
		}
	}

	void "update() authorizes, updates and renders the canvas as json"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.JSON = [
			name: "updated, new name",
			modules: [],
		]
		request.method = "PUT"
		request.requestURI = "/api/v1/canvases/$params.id"
		withFilters(action: "update") {
			controller.update()
		}

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.updateExisting(canvas1, _, me) >> { Canvas canvas, SaveCanvasCommand command, SecUser user ->
			assert command.name == "updated, new name"
			assert command.modules == []
		}
	}

	void "update() must not update canvas if authorization fails"() {
		Map originalProperties = canvas2.properties

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = canvas2.id
		request.JSON = [
			name: "me me me",
			modules: []
		]
		request.method = "PUT"
		request.requestURI = "/api/v1/canvases/$params.id"
		withFilters(action: "update") {
			controller.update()
		}

		then: "canvas must be unchanged"
		originalProperties == canvas2.properties

		and: "exception must not be swallowed"
		thrown NotPermittedException
		1 * canvasService.authorizedGetById(canvas2.id, me, Permission.Operation.WRITE) >> { throw new NotPermittedException("mock") }
	}

	void "delete() must authorize and delete the canvas"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.method = "DELETE"
		request.requestURI = "/api/v1/canvases/$params.id"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		response.status == 204
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.deleteCanvas(canvas1, me)
	}

	void "delete() must not delete the canvas if authorization fails"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "2"
		request.method = "DELETE"
		request.requestURI = "/api/v1/canvases/$params.id"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		thrown NotPermittedException
		1 * canvasService.authorizedGetById("2", me, Permission.Operation.WRITE) >> { throw new NotPermittedException("mock") }
		Canvas.get("2") != null
	}

	void "start() must authorize and start a canvas"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/start"
		withFilters(action: "start") {
			controller.start()
		}

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.start(canvas1, false, me)
	}

	void "start() must authorize and be able to start a Canvas with clearing enabled"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.JSON = [clearState: true]
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/start"
		withFilters(action: "start") {
			controller.start()
		}

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.start(canvas1, true, me)
	}

	void "start() must not start a canvas if authorization fails"() {
		Map originalProperties = canvas2.properties

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = canvas2.id
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/start"
		withFilters(action: "start") {
			controller.start()
		}

		then: "canvas must be unchanged"
		originalProperties == canvas2.properties

		and: "exception must not be swallowed"
		thrown NotPermittedException
		1 * canvasService.authorizedGetById(canvas2.id, me, Permission.Operation.WRITE) >> {throw new NotPermittedException("mock")}
	}

	void "stop() must authorize and stop a canvas"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/stop"
		withFilters(action: "stop") {
			controller.stop()
		}

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.stop(canvas1, me)
	}

	void "stop() must return 204 for adhoc canvases"() {
		canvas1.adhoc = true

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/stop"
		withFilters(action: "stop") {
			controller.stop()
		}

		then:
		response.status == 204
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.stop(canvas1, me)
	}

	void "stop() must not stop the canvas if authorization fails"() {
		Map originalProperties = canvas2.properties

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = canvas2.id
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/stop"
		withFilters(action: "stop") {
			controller.stop()
		}

		then: "canvas must be unchanged"
		originalProperties == canvas2.properties

		and: "exception must not be swallowed"
		thrown NotPermittedException
		1 * canvasService.authorizedGetById(canvas2.id, me, Permission.Operation.WRITE) >> {throw new NotPermittedException("mock")}
	}

	void "stop() must throw an exception if the canvas can't be reached"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/stop"
		withFilters(action: "stop") {
			controller.stop()
		}

		then:
		thrown(CanvasUnreachableException)
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.WRITE) >> canvas1
		1 * canvasService.stop(canvas1, me) >> { throw new CanvasUnreachableException("Test message") }

	}

	void "module() must authorize and render the result as json"() {
		def result = JSON.parse(canvas1.json).modules.find {it.hash == 1}

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.canvasId = "1"
		params.moduleId = 1
		params.dashboardId = "2"
		request.method = "GET"
		request.requestURI = "/api/v1/canvases/$params.id/modules/"
		withFilters(action: "module") {
			controller.module()
		}

		then:
		response.status == 200
		response.json == result
		1 * canvasService.authorizedGetModuleOnCanvas("1", 1, "2", me, Permission.Operation.READ) >> result
	}

	void "module() supports runtime parameter"() {
		def result = JSON.parse(canvas1.json).modules.find {it.hash == 1}

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.canvasId = "1"
		params.moduleId = 1
		params.dashboardId = "2"
		params.runtime = true
		request.method = "GET"
		request.requestURI = "/api/v1/canvases/$params.id/modules/$params.moduleId"
		withFilters(action: "module") {
			controller.module()
		}

		then:
		response.status == 200
		response.json == result
		1 * controller.signalPathService.runtimeRequest(_, false) >> [success:true, json:result]
	}

	void "runtimeRequest() must build and send a runtime request to the canvas"() {
		def runtimeResponse = [foo: 'bar']

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = canvas1.id
		params.path = canvas1.id
		request.JSON = [bar: 'foo']
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/request"
		withFilters(action: "runtimeRequest") {
			controller.runtimeRequest()
		}

		then:
		response.status == 200
		response.json == runtimeResponse
		1 * controller.signalPathService.buildRuntimeRequest([bar: 'foo'], "canvases/$canvas1.id", me)
		1 * controller.signalPathService.runtimeRequest(_, false) >> runtimeResponse
	}

	void "runtimeRequest() must force a local request if params.local is true"() {
		def runtimeResponse = [foo: 'bar']

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = canvas1.id
		params.path = canvas1.id
		params.local = "true"
		request.JSON = [bar: 'foo']
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/$params.id/request"
		withFilters(action: "runtimeRequest") {
			controller.runtimeRequest()
		}

		then:
		response.status == 200
		response.json == runtimeResponse
		1 * controller.signalPathService.runtimeRequest(_, true) >> runtimeResponse
	}

}
