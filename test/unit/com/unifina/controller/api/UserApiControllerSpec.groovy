package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.domain.security.SecUser
import grails.test.mixin.TestFor

@TestFor(UserApiController)
class UserApiControllerSpec extends ControllerSpecification {

	SecUser me

	def setup() {
		me = new SecUser(
			id: 1,
			name: "me",
			username: "me@too.com",
			enabled: true,
		)
	}

	void "unauthenticated user gets back 401"() {
		when:
		unauthenticated {
			controller.getUserInfo()
		}
		then:
		response.status == 401
	}

	void "authenticated user gets back specified user info from /me"() {
		when:
		authenticatedAs(me) { controller.getUserInfo() }
		then:
		response.json.name == me.name
		response.json.username == me.username
		!response.json.hasProperty("password")
		!response.json.hasProperty("id")
	}
}
