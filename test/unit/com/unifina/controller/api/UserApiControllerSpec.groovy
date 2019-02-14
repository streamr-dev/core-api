package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.domain.security.SecUser
import com.unifina.service.UserService
import grails.test.mixin.TestFor

@TestFor(UserApiController)
class UserApiControllerSpec extends ControllerSpecification {

	SecUser me

	def setup() {
		me = new SecUser(
			name: "me",
			username: "me@too.com",
			enabled: true,
		)
		me.id = 1
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

	void "delete user account"() {
		setup:
		controller.userService = Mock(UserService)

		when:
		request.apiUser = me
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me/1"
		params.id = me.id
		authenticatedAs(me) { controller.delete(me.id) }

		then:
		1 * controller.userService.delete(me, me.id)
		response.status == 204
	}
}
