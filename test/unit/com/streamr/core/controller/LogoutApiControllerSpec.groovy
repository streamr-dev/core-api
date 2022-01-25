package com.streamr.core.controller

import com.streamr.core.domain.User
import com.streamr.core.service.EthereumUserService
import com.streamr.core.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(LogoutApiController)
@Mock([User])
class LogoutApiControllerSpec extends ControllerSpecification {

	SessionService sessionService
	EthereumUserService ethereumUserService
	User me

	def setup() {
		me = new User().save(failOnError: true, validate: false)
		sessionService = controller.sessionService = mockBean(SessionService, Mock(SessionService))
		ethereumUserService = mockBean(EthereumUserService, Mock(EthereumUserService))
	}

	def "logout invalidates session token"() {
		when:
		request.addHeader("Authorization", "BEARER session-token")
		request.method = "POST"
		authenticatedAs(me) { controller.logout() }
		then:
		response.status == 200
		response.json == [:]
		1 * sessionService.invalidateSession("session-token")
	}

	def "logout throws if not logged in"() {
		when:
		request.addHeader("Authorization", "BEARER session-token")
		request.method = "POST"
		unauthenticated() { controller.logout() }
		then:
		response.status == 401
	}
}
