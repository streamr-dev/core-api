package com.unifina.controller.api

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.CanvasService
import com.unifina.service.UnifinaSecurityService
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import groovy.json.JsonBuilder
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

@TestFor(CanvasApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, Canvas, UnifinaCoreAPIFilters, UnifinaSecurityService, SpringSecurityService])
class CanvasApiControllerSpec extends Specification {

	CanvasService canvasService
	SecUser me
	Canvas canvas1
	Canvas canvas2
	Canvas canvas3
	Canvas canvas4

	void setup() {
		controller.canvasService = canvasService = Mock(CanvasService)
		controller.unifinaSecurityService = mainContext.getBean(UnifinaSecurityService)

		me = new SecUser(id: 1, apiKey: "myApiKey").save(validate: false)
		SecUser other = new SecUser(id: 2, apiKey: "otherApiKey").save(validate: false)

		canvas1 = new Canvas(
			user: me,
			name: "mine",
			json: new JsonBuilder([name: "mine", modules: [], settings: [:]]).toString(),
			state: Canvas.State.STOPPED,
			hasExports: false
		)
		canvas1.save(validate: true, failOnError: true)

		canvas2 = new Canvas(
			user: other,
			name: "not mine",
			json: '{name: "not mine", modules: []}',
			state: Canvas.State.STOPPED,
			hasExports: false
		).save(validate: true, failOnError: true)

		canvas3 = new Canvas(
			user: other,
			name: "not mine but example",
			json: '{name: "not mine but example", modules: []}',
			state: Canvas.State.STOPPED,
			example: true,
			hasExports: false
		).save(validate: true, failOnError: true)

		assert SecUser.count() == 2
		assert Canvas.count() == 3
	}

	void "can list all my Canvases"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 3
		1 * canvasService.findAllBy(me, null, null, null) >> [canvas1, canvas2, canvas3]
		0 * canvasService._

	}

	void "must be able to load my own Canvas"() {
		when:
		params.id = "1"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/show"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json == [
			id: "1",
			name: "mine",
			state: "STOPPED",
			hasExports: false,
			serialized: false,
			settings: [:],
			modules: [],
			adhoc: false,
			updated: JSONObject.NULL,
			created: JSONObject.NULL,
			uiChannel: JSONObject.NULL,
		]

		1 * canvasService.reconstruct(canvas1) >> { Canvas c -> JSON.parse(c.json) }
		0 * canvasService._
	}

	void "must be able to save a new Canvas"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.JSON = [
			name: "brand new Canvas",
			modules: [],
		]
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/save"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 200
		1 * canvasService.createNew(_, me) >> { SaveCanvasCommand command, SecUser user ->
			assert command.name == "brand new Canvas"
			assert command.modules == []
			return new Canvas(json: "{}")
		}
		0 * canvasService._
	}

	void "must not be able to load others' Canvases"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "2"
		request.requestURI = "/api/v1/canvases/show"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
		0 * canvasService._
	}

	void "must be able to load example, even if it's not mine"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "3"
		request.requestURI = "/api/v1/canvases/show"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json == JSON.parse(new JsonBuilder(canvas3.toMap()).toString())
		1 * canvasService.reconstruct(canvas3) >> { Canvas c -> JSON.parse(c.json) }
		0 * canvasService._
	}

	void "must be able to overwrite my own Canvas"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.JSON = [
			name: "updated, new name",
			modules: [],
		]
		request.requestURI = "/api/v1/canvases/update"
		withFilters(action: "update") {
			controller.update()
		}

		then:
		1 * canvasService.updateExisting(canvas1, _) >> { Canvas canvas, SaveCanvasCommand command ->
			assert command.name == "updated, new name"
			assert command.modules == []
		}
		0 * canvasService._
	}

	void "must not be able overwrite others' Canvases"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "2"
		request.JSON = [
			name: "me me me",
			modules: []
		]
		request.requestURI = "/api/v1/canvases/update"
		withFilters(action: "update") {
			controller.update()
		}
		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
	}

	void "must not be able overwrite example"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "3"
		params.json = [
		    name: "me me me",
			modules: []
		]
		request.requestURI = "/api/v1/canvases/update"
		withFilters(action: "update") {
			controller.update()
		}
		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
	}

	void "must be able to delete my own Canvas"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.requestURI = "/api/v1/canvases/delete"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		Canvas.get("1") == null
	}

	void "must not be able delete others' Canvases"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "2"
		request.requestURI = "/api/v1/canvases/delete"
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
	}

	void "must not be able delete example"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "3"
		request.requestURI = "/api/v1/canvases/delete"
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
	}
}
