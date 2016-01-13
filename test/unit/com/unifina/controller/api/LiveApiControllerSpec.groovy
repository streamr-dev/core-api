package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.UnifinaSecurityService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import groovy.json.JsonSlurper
import spock.lang.Specification
@Mixin(FiltersUnitTestMixin)

@TestFor(LiveApiController)
@Mock([SecUser, RunningSignalPath, UnifinaCoreAPIFilters, Module, UiChannel, UnifinaSecurityService, SpringSecurityService])
class LiveApiControllerSpec extends Specification {

	void "index doesn't return the uiChannels without module or module.webcomponent"() {
		setup:
		def user = new SecUser(id: 1, apiKey: "myKey")
		user.save(validate: false)
		def rsp = new RunningSignalPath(id: user.id, user: user, adhoc: false)
		rsp.save(validate: false)

		// UiChannel without module
		def ui1 = new UiChannel(runningSignalPath: rsp, name:"1")
		ui1.save()

		// UiChannel with module without webcomponent
		def module1 = new Module(id: 1)
		module1.save(validate: false)
		def ui2 = new UiChannel(name: "2", runningSignalPath: rsp, module: module1)
		ui2.save(validate: false)

		// UiChannel with module with webcomponent
		def module2 = new Module(id: 2, webcomponent: "test")
		module2.save(validate: false)
		def ui3 = new UiChannel(name:"3", runningSignalPath: rsp, module: module2)
		ui3.save(validate: false)

		// Another UiChannel with module with webcomponent
		def module3 = new Module(id: 3, webcomponent: "test2")
		module3.save(validate: false)
		def ui4 = new UiChannel(name:"4", runningSignalPath: rsp, module: module3)
		ui4.save(validate: false)

		when: "ask for the listJson"
		request.addHeader("Authorization", "Token myKey")
		request.method = "GET"
		request.requestURI = "/api/v1/live"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.json != null
		Map json = new JsonSlurper().parseText(response.json.toString())[0]
		json.uiChannels.size() == 2
		// For some reason couldn't save the ids for the uiChannels so we check the names instead
		json.uiChannels.get(0).name == "3"
		json.uiChannels.get(1).name == "4"
	}
}
