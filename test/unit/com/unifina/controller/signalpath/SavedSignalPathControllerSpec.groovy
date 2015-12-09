package com.unifina.controller.signalpath

import static org.junit.Assert.*
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.support.*
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.service.SignalPathService
import com.unifina.service.UnifinaSecurityService
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals

@TestFor(SavedSignalPathController)
@Mock([SecUser, SavedSignalPath])
class SavedSignalPathControllerSpec extends Specification {

	SavedSignalPath ssp1
	SavedSignalPath ssp2
	SavedSignalPath ssp3
	SavedSignalPath ssp4
	
	void setup() {
		// Populate db
		
		SecUser me = new SecUser(id:1).save(validate:false)
		SecUser other = new SecUser(id:2).save(validate:false)
		
		ssp1 = new SavedSignalPath(id:1, user:me, name:"mine", json:"{\"signalPathData\":{\"name\":\"mine\"}}", type:SavedSignalPath.TYPE_USER_SIGNAL_PATH).save(validate:false)
		ssp2 = new SavedSignalPath(id:2, user:other, name:"not mine", json:"{\"signalPathData\":{\"name\":\"not mine\"}}", type:SavedSignalPath.TYPE_USER_SIGNAL_PATH).save(validate:false)
		ssp3 = new SavedSignalPath(id:3, user:other, name:"not mine but example", json:"{\"signalPathData\":{\"name\":\"not mine but example\"}}", type:SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH).save(validate:false)
		ssp4 = new SavedSignalPath(id:4, user:me, name:"my example", json:"{\"signalPathData\":{\"name\":\"not mine but example\"}}", type:SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH).save(validate:false)
		
		assert SecUser.count()==2
		assert SavedSignalPath.count()==4
		
		
		// Mock services or use real ones
		
		controller.unifinaSecurityService = new UnifinaSecurityService()
		controller.springSecurityService = [getCurrentUser: {-> me}] as SpringSecurityService
		controller.unifinaSecurityService.springSecurityService = controller.springSecurityService
		controller.signalPathService = [
			reconstruct: {json, globals -> return json},
			jsonToSignalPath: {Map signalPathData, boolean connectionsReady, Globals globals, boolean isRoot->
				return new SignalPath()
			},
			signalPathToJson: {SignalPath sp->
				return [:]
			}
		] as SignalPathService
	}
	
	void "must be able to load my own SignalPath"() {
		when:
			params.id = "1"
			request.method = "GET"
			webRequest.actionName = "load"
			if (controller.beforeInterceptor.action.doCall([actionUri:new URI("http://localhost/savedSignalPath/load")]))
				controller.load()
		then:
			response.json.signalPathData.name == "mine"
			response.json.saveData.isSaved == true
	}
	
	void "must be able to save a new SignalPath"() {
		when:
			params.name = "new sp"
			params.json = ssp1.json
			request.method = "POST"
			webRequest.actionName = "save"
			if (controller.beforeInterceptor.action.doCall())
				controller.save()
		then:
			response.json.isSaved
	}
	
	void "must not be able to load others' SignalPath"() {
		when:
			params.id = "2"
			request.method = "GET"
			webRequest.actionName = "load"
		then:
			!controller.beforeInterceptor.action.doCall([actionUri:new URI("http://localhost/savedSignalPath/load")])
	}
	
	void "must be able to load example, even if it's not mine"() {
		when:
			params.id = "3"
			request.method = "GET"
			webRequest.actionName = "load"
			if (controller.beforeInterceptor.action.doCall([actionUri:new URI("http://localhost/savedSignalPath/load")]))
				controller.load()
		then:
			response.json.signalPathData.name == "not mine but example"
		then: "it must not have saveData.isSaved==true"
			!response.json.saveData?.isSaved
	}
	
	void "my own example must have saveData"() {
		when:
			params.id = "4"
			request.method = "GET"
			webRequest.actionName = "load"
			if (controller.beforeInterceptor.action.doCall())
				controller.load()
		then:
			response.json.signalPathData.name == "my example"
			response.json.saveData.isSaved
	}
	
	void "must be able to overwrite my own SignalPath"() {
		when:
			params.id = "1"
			params.name = "new name"
			params.json = ssp1.json
			request.method = "POST"
			webRequest.actionName = "save"
			if (controller.beforeInterceptor.action.doCall())
				controller.save()
		then:
			response.json.isSaved
			SavedSignalPath.get(1).name == "new name"
	}
	
	void "must not be able overwrite others' SignalPath"() {
		when:
			params.id = "3"
			params.name = "new name"
			params.json = ssp1.json
			request.method = "POST"
			webRequest.actionName = "save"
		then:
			!controller.beforeInterceptor.action.doCall()
	}

}
