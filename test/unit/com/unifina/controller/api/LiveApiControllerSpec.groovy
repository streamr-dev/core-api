package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
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
@Mock([SecUser, Canvas, UnifinaCoreAPIFilters, Module, UiChannel, UnifinaSecurityService, SpringSecurityService])
class LiveApiControllerSpec extends Specification {

	Canvas c1
	Canvas c2

	void setup() {
		SecUser me = new SecUser(
			username: "me@me.com",
			password: "pass",
			apiKey: "myKey",
			timezone: "Europe/Helsinki",
			name: "my name",
		).save(failOnError: true)

		SecUser someoneElse = new SecUser(
			username: "someoneElse@someoneElse.com",
			password: "pass",
			apiKey: "someoneElsesApiKey",
			timezone: "Europe/Helsinki",
			name: "my name",
		).save(failOnError: true)

		c1 = new Canvas(
			name: "canvas-1",
			user: me,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		c2 = new Canvas(
			name: "canvas-2",
			user: me,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		Canvas c3 = new Canvas(
			name: "canvas-3",
			user: me,
			json: "{}",
			adhoc: true,
			state: Canvas.State.RUNNING
		).save(failOnError: true)

		Canvas c4 = new Canvas(
			name: "canvas-4",
			user: me,
			json: "{}",
			state: Canvas.State.RUNNING
		).save(failOnError: true)

		Canvas c5 = new Canvas(
			name: "canvas-5",
			user: me,
			json: "{}"
		).save(failOnError: true)

		Canvas c6 = new Canvas(
			name: "canvas-6",
			user: someoneElse,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		Canvas c7 = new Canvas(
			name: "canvas-7",
			user: someoneElse,
			json: "{}"
		).save(failOnError: true)

		// UiChannel without module
		UiChannel ui1 = new UiChannel(canvas: c1, name:"1").save()

		// UiChannel with module without webcomponent
		def module1 = new Module().save(validate: false)
		def ui2 = new UiChannel(name: "2", canvas: c1, module: module1)
		ui2.save(validate: false)

		// UiChannel with module with webcomponent
		def module2 = new Module(webcomponent: "test").save(validate: false)
		def ui3 = new UiChannel(name:"3", canvas: c1, module: module2)
		ui3.save(validate: false)

		// Another UiChannel with module with webcomponent
		def module3 = new Module(webcomponent: "test2").save(validate: false)
		def ui4 = new UiChannel(name:"4", canvas: c1, module: module3)
		ui4.save(validate: false)
	}

	void "index returns current user's live canvases"() {
		when:
		request.addHeader("Authorization", "Token myKey")
		request.method = "GET"
		request.requestURI = "/api/v1/live"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.json.size() == 2
		response.json.collect { it.id } == [c1.id, c2.id]
		response.json.collect { it.name } == [c1.name, c2.name]
		response.json.collect { it.state.name } == [c1.state.name(), c2.state.name()]
		response.json.collect { it.uiChannels }.size() == 2
	}

	void "index doesn't return the uiChannels without module or module.webcomponent"() {
		when:
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
		json.uiChannels.get(0).name == "3"
		json.uiChannels.get(1).name == "4"
		json.uiChannels.get(0).module == [id: 2, webcomponent: "test"]
		json.uiChannels.get(1).module == [id: 3, webcomponent: "test2"]
	}
}
