package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.SavedSignalPath
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
@Mock([SecUser, SavedSignalPath, UnifinaCoreAPIFilters, UnifinaSecurityService, SpringSecurityService])
class CanvasApiControllerSpec extends Specification {


	SavedSignalPath ssp1
	SavedSignalPath ssp2
	SavedSignalPath ssp3
	SavedSignalPath ssp4

	void setup() {
		SecUser me = new SecUser(id: 1, apiKey: "myApiKey").save(validate: false)
		SecUser other = new SecUser(id: 2, apiKey: "otherApiKey").save(validate: false)

		ssp1 = new SavedSignalPath(
			user: me,
			name: "mine",
			json: '{name: "mine", modules: []}',
			type: SavedSignalPath.TYPE_USER_SIGNAL_PATH,
			hasExports: false
		).save(validate: true, failOnError: true)

		ssp2 = new SavedSignalPath(
			user: other,
			name: "not mine",
			json: '{name: "not mine", modules: []}',
			type: SavedSignalPath.TYPE_USER_SIGNAL_PATH,
			hasExports: false
		).save(validate: true, failOnError: true)

		ssp3 = new SavedSignalPath(
			user: other,
			name: "not mine but example",
			json: '{name: "not mine but example", modules: []}',
			type: SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH,
			hasExports: false
		).save(validate: true, failOnError: true)

		ssp4 = new SavedSignalPath(
			user: me,
			name: "my example",
			json: '{name: "not mine but example", modules: []}',
			type: SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH,
			hasExports: false
		).save(validate: true, failOnError: true)

		assert SecUser.count() == 2
		assert SavedSignalPath.count() == 4

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
		request.method = "GET"
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
		request.method = "GET"
		webRequest.actionName = "load"
		request.requestURI = "/api/v1/canvases/load"
		withFilters(action: "load") {
			controller.load()
		}
		then:
		response.status == 200
		response.json.uuid.size() == 22
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
		webRequest.actionName = "save"
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
		request.method = "GET"
		webRequest.actionName = "load"
		request.requestURI = "/api/v1/canvases/load"
		withFilters(action: "load") {
			controller.load()
		}
		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
	}

	void "must be able to load example, even if it's not mine"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "3"
		request.method = "GET"
		webRequest.actionName = "load"
		request.requestURI = "/api/v1/canvases/load"
		withFilters(action: "load") {
			controller.load()
		}
		then:
		response.status == 200
		response.json.name == "not mine but example"
	}

	void "my own example must have saveData"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "4"
		request.method = "GET"
		webRequest.actionName = "load"
		request.requestURI = "/api/v1/canvases/load"
		withFilters(action: "load") {
			controller.load()
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
		request.method = "POST"
		webRequest.actionName = "save"
		request.requestURI = "/api/v1/canvases/save"
		withFilters(action: "save") {
			controller.save()
		}
		then:
		SavedSignalPath.get(1).name == "updated, new name"
	}

	void "must not be able overwrite others' SignalPath"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = "3"
		params.name = "new name"
		params.json = ssp1.json
		request.method = "POST"
		withFilters(action: "save") {
			controller.save()
		}
		then:
		response.status == 403
		response.json.code == "FORBIDDEN"
	}

}
