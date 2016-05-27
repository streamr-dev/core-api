package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.signalpath.SignalPathRunner
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SignalPathService)
@Mock([SecUser, Canvas])
class SignalPathServiceSpec extends Specification {

	Canvas c1

	def setup() {
		SecUser me = new SecUser(username: "a@a.com", password: "pw", name: "name", timezone: "Europe/Helsinki")
		me.save(failOnError: true)

		c1 = new Canvas(
			name: "canvas-1",
			user: me,
			json: "{}",
			serialized: new byte[512],
			serializationTime: new Date()
		).save(failOnError: true)
	}

	def "clearState() clears serialized state"() {
		when:
		service.clearState(c1)

		then:
		Canvas.findById(c1.id).serialized == null
		Canvas.findById(c1.id).serializationTime == null
	}

	def "stopLocalRunner() sets state to STOPPED if runner is not found"() {
		def service = Spy(SignalPathService)
		service.servletContext = service.servletContext ?: [:]
		service.servletContext["signalPathRunners"] = [:]

		when:
		boolean success = service.stopLocalRunner("id")

		then:
		1 * service.updateState("id", Canvas.State.STOPPED) >> null
		!success
	}

	def "stopLocalRunner() tries to stop the runner if found and alive"() {
		def runner = Mock(SignalPathRunner)
		service.servletContext = service.servletContext ?: [:]
		service.servletContext.signalPathRunners = [id: runner]

		when:
		boolean success = service.stopLocalRunner("id")

		then: "try to stop"
		1 * runner.abort()
		and: "stopped successfully"
		1 * runner.getRunning() >> false
		success
	}

	def "stopLocalRunner() returns false if the canvas doesn't stop"() {
		def runner = Mock(SignalPathRunner)
		service.servletContext = service.servletContext ?: [:]
		service.servletContext.signalPathRunners = [id: runner]

		when:
		boolean success = service.stopLocalRunner("id")

		then: "try to stop"
		1 * runner.abort()
		and: "still running"
		1 * runner.getRunning() >> true
		!success
	}

}
