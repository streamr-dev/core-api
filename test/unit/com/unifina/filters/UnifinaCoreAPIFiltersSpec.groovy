package com.unifina.filters

import com.unifina.BeanMockingSpecification
import com.unifina.controller.api.NodeApiController
import com.unifina.domain.security.Key
import com.unifina.domain.security.Role
import com.unifina.domain.security.User
import com.unifina.domain.security.UserRole
import com.unifina.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin

@TestMixin(FiltersUnitTestMixin)
@TestFor(NodeApiController)
@Mock([User, UserRole, UnifinaCoreAPIFilters])
class UnifinaCoreAPIFiltersSpec extends BeanMockingSpecification {

	User user
	Role adminRole, devopsRole
	SessionService sessionService

	void setup() {
		user = new User().save(failOnError: true, validate: false)
		sessionService = mockBean(SessionService, Mock(SessionService))

		Key key = new Key(name: "k1", user: user)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		adminRole = new Role(authority: "ROLE_ADMIN").save(failOnError: true)
		devopsRole = new Role(authority: "ROLE_DEV_OPS").save(failOnError: true)
	}

	void "authenticationFilter responds with 403 and 'NOT_PERMITTED' if user don't have proper role"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/nodes"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 403
		response.json == [
			code   : "NOT_PERMITTED",
			message: "Not authorized to access this endpoint"
		]
	}

	void "authenticationFilter passes if user has proper role"() {
		setup:
		new UserRole(user: user, role: adminRole).save(failOnError: true)

		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/nodes"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
	}

	void "authentication passes when session token provided"() {
		String token = "mytoken"
		when:
		new UserRole(user: user, role: adminRole).save(failOnError: true)
		request.addHeader("Authorization", "Bearer " + token)
		request.requestURI = "/api/v1/nodes"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * sessionService.getUserishFromToken(token) >> user
		response.status == 200
	}
}
