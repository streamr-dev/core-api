package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
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
			serialized: "here_be_content",
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
}
