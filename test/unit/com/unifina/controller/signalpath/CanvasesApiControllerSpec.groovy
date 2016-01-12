package com.unifina.controller.signalpath

import com.unifina.controller.api.CanvasesApiController
import grails.test.mixin.web.FiltersUnitTestMixin
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.service.SignalPathService
import com.unifina.service.UnifinaSecurityService
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.filters.UnifinaCoreAPIFilters

@TestFor(CanvasesApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, SavedSignalPath, UnifinaCoreAPIFilters, UnifinaSecurityService, SpringSecurityService])
class CanvasesApiControllerSpec extends Specification {


	SavedSignalPath ssp1
	SavedSignalPath ssp2
	SavedSignalPath ssp3
	SavedSignalPath ssp4
	
	void setup() {
		// Populate db
		
		SecUser me = new SecUser(id:1, apiKey: "myApiKey").save(validate:false)
		SecUser other = new SecUser(id:2, apiKey: "otherApiKey").save(validate:false)
		
		ssp1 = new SavedSignalPath(id:1, user:me, name:"mine", json:"{\"signalPathData\":{\"name\":\"mine\"}}", type:SavedSignalPath.TYPE_USER_SIGNAL_PATH).save(validate:false)
		ssp2 = new SavedSignalPath(id:2, user:other, name:"not mine", json:"{\"signalPathData\":{\"name\":\"not mine\"}}", type:SavedSignalPath.TYPE_USER_SIGNAL_PATH).save(validate:false)
		ssp3 = new SavedSignalPath(id:3, user:other, name:"not mine but example", json:"{\"signalPathData\":{\"name\":\"not mine but example\"}}", type:SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH).save(validate:false)
		ssp4 = new SavedSignalPath(id:4, user:me, name:"my example", json:"{\"signalPathData\":{\"name\":\"not mine but example\"}}", type:SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH).save(validate:false)
		
		assert SecUser.count()==2
		assert SavedSignalPath.count()==4
		
		
		// Mock services or use real ones

		/*
		controller.unifinaSecurityService = new UnifinaSecurityService()
		controller.springSecurityService = [getCurrentUser: {-> me}] as SpringSecurityService
		controller.unifinaSecurityService.springSecurityService = controller.springSecurityService*/
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
			withFilters([action: "index"]) {
				controller.index()
			}
		then:
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
			withFilters([action: "load"]) {
				controller.load()
			}
		then:
			response.json.signalPathData.name == "mine"
			response.json.saveData.isSaved == true
	}
	
	void "must be able to save a new SignalPath"() {
		when:
			request.addHeader("Authorization", "Token myApiKey")
			params.json = [
			    name: "brand new SavedSignalPath",
				modules: [],
			]
			request.method = "POST"
			webRequest.actionName = "save"
			request.requestURI = "/api/v1/canvases/save"
			withFilters([action: "save"]) {
				controller.save()
			}
		then:
			response.json.uuid.size() > 10
	}
	
	void "must not be able to load others' SignalPath"() {
		when:
			request.addHeader("Authorization", "Token myApiKey")
			params.id = "2"
			request.method = "GET"
			webRequest.actionName = "load"
			request.requestURI = "/api/v1/canvases/load"
			withFilters([action: "load"]) {
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
			withFilters([action: "load"]) {
				controller.load()
			}
		then:
			response.json.signalPathData.name == "not mine but example"
		then: "it must not have saveData.isSaved==true"
			!response.json.saveData?.isSaved
	}
	
	void "my own example must have saveData"() {
		when:
			request.addHeader("Authorization", "Token myApiKey")
			params.id = "4"
			request.method = "GET"
			webRequest.actionName = "load"
			request.requestURI = "/api/v1/canvases/load"
			withFilters([action: "load"]) {
				controller.load()
			}
		then:
			response.json.signalPathData.name == "my example"
			response.json.saveData.isSaved
	}
	
	void "must be able to overwrite my own SignalPath"() {
		when:
			request.addHeader("Authorization", "Token myApiKey")
			params.id = "1"
			params.json = [
				name: "updated, new name",
				modules: [],
			]
			request.method = "POST"
			webRequest.actionName = "save"
			request.requestURI = "/api/v1/canvases/save"
			withFilters([action: "save"]) {
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
			withFilters([action: "save"]) {
				controller.save()
			}
		then:
			response.status == 403
			response.json.code == "FORBIDDEN"
	}

}
