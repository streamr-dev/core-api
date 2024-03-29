package com.streamr.core.controller

import com.streamr.core.BeanMockingSpecification
import com.streamr.core.domain.Role
import com.streamr.core.domain.User
import com.streamr.core.domain.UserRole
import com.streamr.core.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin

@TestMixin(FiltersUnitTestMixin)
@TestFor(NodeApiController)
@Mock([User, UserRole, RESTAPIFilters, Role])
class RESTAPIFiltersSpec extends BeanMockingSpecification {
	private static final String USER_SESSION_TOKEN = "mytoken"

	User user
	Role adminRole
	Role devopsRole
	SessionService sessionService

	void setup() {
		user = new User().save(failOnError: true, validate: false)
		sessionService = mockBean(SessionService, Mock(SessionService))

		adminRole = new Role(authority: "ROLE_ADMIN").save(failOnError: true)
		devopsRole = new Role(authority: "ROLE_DEV_OPS").save(failOnError: true)
	}

	void "authenticationFilter responds with 403 and 'NOT_PERMITTED' if user don't have proper role"() {
		when:
		request.addHeader("Authorization", "Bearer " + USER_SESSION_TOKEN)
		request.requestURI = "/api/v2/nodes"
		withFilters(action: "config") {
			controller.config()
		}

		then:
		1 * sessionService.getUserFromToken(USER_SESSION_TOKEN) >> user
		response.status == 403
		response.json == [
			code: "NOT_PERMITTED",
			message: "Not authorized to access this endpoint"
		]
	}

	void "authenticationFilter passes if user has proper role"() {
		setup:
		new UserRole(user: user, role: adminRole).save(failOnError: true)

		when:
		request.addHeader("Authorization", "Bearer " + USER_SESSION_TOKEN)
		request.requestURI = "/api/v2/nodes"
		withFilters(action: "config") {
			controller.config()
		}

		then:
		1 * sessionService.getUserFromToken(USER_SESSION_TOKEN) >> user
		response.status == 200
	}

	void "authentication passes when session token provided"() {
		when:
		new UserRole(user: user, role: adminRole).save(failOnError: true)
		request.addHeader("Authorization", "Bearer " + USER_SESSION_TOKEN)
		request.requestURI = "/api/v2/nodes"
		withFilters(action: "config") {
			controller.config()
		}

		then:
		1 * sessionService.getUserFromToken(USER_SESSION_TOKEN) >> user
		response.status == 200
	}
}
