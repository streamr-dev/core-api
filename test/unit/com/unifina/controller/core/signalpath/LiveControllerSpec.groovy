package com.unifina.controller.core.signalpath

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import spock.lang.Specification

@TestFor(LiveController)
@Mock([SecUser, RunningSignalPath, Module, UiChannel])
class LiveControllerSpec extends Specification {
	void setup() {
		defineBeans {
			serializationService(SerializationService)
			signalPathService(SignalPathService) { it.autowire = true }
		}
		controller.signalPathService = mainContext.getBean(SignalPathService)
	}

	private void mockSpringSecurityService(user) {
		def springSecurityService = [
				getCurrentUser: {-> user },
				encodePassword: {String pw-> pw+"-encoded" }
		] as SpringSecurityService
		controller.springSecurityService = springSecurityService
	}

	void "deserialization failure results in error message"() {
		setup: "runningSignalPath has been incorrectly serialized to database"
		def rsp = new RunningSignalPath(
			name: "name",
			adhoc: false,
			serialized: "{invalid: 'serialization'}"
		)
		rsp.save(failOnError: true, validate: false)

		when: "runningSignalPath is started"
		params.id = rsp.id
		controller.start()

		then: "error flash message is shown"
		flash.error.length() > 0
	}

	void "getListJson doesn't return the uiChannels without module or module.webcomponent"() {
		setup:
		def user = new SecUser(id: 1)
		user.save(validate: false)
		def rsp = new RunningSignalPath(id: 1, user: user, adhoc: false)
		rsp.save(validate: false)

		// UiChannel without module
		def ui1 = new UiChannel(runningSignalPath: 1, name:"1")
		ui1.save()

		// UiChannel with module without webcomponent
		def module1 = new Module(id: 1)
		module1.save(validate: false)
		def ui2 = new UiChannel(name: "2", runningSignalPath: 1, module: 1)
		ui2.save(validate: false)

		// UiChannel with module with webcomponent
		def module2 = new Module(id: 2, webcomponent: "test")
		module2.save(validate: false)
		def ui3 = new UiChannel(name:"3", runningSignalPath: 1, module: 2)
		ui3.save(validate: false)

		// Another UiChannel with module with webcomponent
		def module3 = new Module(id: 3, webcomponent: "test2")
		module3.save(validate: false)
		def ui4 = new UiChannel(name:"4", runningSignalPath: 1, module: 3)
		ui4.save(validate: false)

		mockSpringSecurityService(user)

		when: "ask for the listJson"
		request.method = "GET"
		controller.getListJson()

		then:
		response.json != null
		Map json = new JsonSlurper().parseText(response.json.toString())[0]
		json.uiChannels.size() == 2
		// For some reason couldn't save the ids for the uiChannels so we check the names instead
		json.uiChannels.get(0).name == "3"
		json.uiChannels.get(1).name == "4"

	}
}