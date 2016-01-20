package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CanvasService)
@Mock([SecUser, Canvas, ModuleService, SpringSecurityService, SignalPathService])
class CanvasServiceSpec extends Specification {

	SecUser me
	SecUser someoneElse

	def setup() {
		me = new SecUser(username: "me@me.com", apiKey: "myKey").save(validate: false)

		new Canvas(
			name: "my_canvas_1",
			user: me,
			json: "{}",
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_2",
			user: me,
			json: "{}",
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_3",
			user: me,
			json: "{}",
			adhoc: false,
			state: Canvas.State.RUNNING
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_4",
			user: me,
			json: "{}",
			adhoc: true,
			state: Canvas.State.RUNNING
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_5",
			user: me,
			json: "{}",
			adhoc: true,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_6",
			user: me,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		someoneElse = new SecUser(username: "someone@someone.com", apiKey: "otherKey").save(validate: false)

		new Canvas(
			name: "someoneElses_canvas_1",
			user: someoneElse,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		new Canvas(
			name: "someoneElses_canvas_2",
			user: someoneElse,
			json: "{}",
		).save(failOnError: true)
	}

	def "findAllBy by default lists all of current user's Canvases"() {
		expect:
		service.findAllBy(me, null, null, null)*.name == (1..6).collect { "my_canvas_" + it }
	}

	def "findAllBy can filter by name"() {
		expect:
		service.findAllBy(me, "my_canvas_4", null, null)*.name == ["my_canvas_4"]
	}

	def "findAllBy can filter by adhoc"() {
		expect:
		service.findAllBy(me, null, false, null)*.name == ["my_canvas_3", "my_canvas_6"]
		service.findAllBy(me, null, true, null)*.name == ["my_canvas_4", "my_canvas_5"]
	}

	def "findAllBy can filter by state"() {
		expect:
		service.findAllBy(me, null, null, Canvas.State.RUNNING)*.name == ["my_canvas_3", "my_canvas_4"]
	}
}
