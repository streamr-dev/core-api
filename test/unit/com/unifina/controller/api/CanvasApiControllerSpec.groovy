package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.SignalPathService
import com.unifina.service.UnifinaSecurityService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import spock.lang.Specification

@TestFor(CanvasApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, Canvas, UnifinaCoreAPIFilters, UnifinaSecurityService, SpringSecurityService])
class CanvasApiControllerSpec extends Specification {

	Canvas canvas1
	Canvas canvas2
	Canvas canvas3
	Canvas canvas4

	void setup() {

		SecUser me = new SecUser(id: 1, apiKey: "myApiKey").save(validate: false)
		SecUser other = new SecUser(id: 2, apiKey: "otherApiKey").save(validate: false)

		canvas1 = new Canvas(
			user: me,
			name: "mine",
			json: '{name: "mine", modules: []}',
			type: Canvas.Type.TEMPLATE,
			hasExports: false
		)
		canvas1.save(validate: true, failOnError: true)

		canvas2 = new Canvas(
			user: other,
			name: "not mine",
			json: '{name: "not mine", modules: []}',
			type: Canvas.Type.TEMPLATE,
			hasExports: false
		).save(validate: true, failOnError: true)

		canvas3 = new Canvas(
			user: other,
			name: "not mine but example",
			json: '{name: "not mine but example", modules: []}',
			type: Canvas.Type.TEMPLATE,
			hasExports: false
		).save(validate: true, failOnError: true)

		canvas4 = new Canvas(
			user: me,
			name: "my example",
			json: '{name: "not mine but example", modules: []}',
			type: Canvas.Type.TEMPLATE,
			hasExports: false
		).save(validate: true, failOnError: true)

		assert SecUser.count() == 2
		assert Canvas.count() == 4

		controller.unifinaSecurityService = mainContext.getBean("unifinaSecurityService")
		controller.signalPathService = [
			reconstruct: { json, globals ->
				json.hasExports = false
				return json
			},
		] as SignalPathService
	}

	void "can list all my SignalPaths"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 2
		response.json.collect { it.name } == ["mine", "my example"]

	}

	void "must be able to load my own SignalPath"() {
		when:
		params.id = "1"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/show"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json.id.size() == 22
		response.json.name == "mine"
		response.json.modules == []
		!response.json.hasExports
	}

	void "must be able to save a new SignalPath"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.json = [
			name   : "brand new SavedSignalPath",
			modules: [],
		]
		request.method = "POST"
		request.requestURI = "/api/v1/canvases/save"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 200
		response.json.uuid.size() > 10
	}

	void "must not be able to load others' SignalPath"() {
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
		response.json.name == "not mine but example"
	}

	void "my own example must have saveData"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "4"
		request.requestURI = "/api/v1/canvases/show"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json.name == "my example"
	}

	void "must be able to overwrite my own SignalPath"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		params.json = [
			name   : "updated, new name",
			modules: [],
		]
		request.requestURI = "/api/v1/canvases/update"
		withFilters(action: "update") {
			controller.update()
		}

		then:
		Canvas.get(1).name == "updated, new name"
	}

	void "must not be able overwrite others' SignalPath"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "2"
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

	void "must be able to delete my own SignalPath"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "1"
		request.requestURI = "/api/v1/canvases/delete"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		SavedSignalPath.get(1) == null
	}

	void "must not be able delete others' SignalPath"() {
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
