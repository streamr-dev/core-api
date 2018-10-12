package com.unifina.filters

import com.unifina.controller.api.NodeApiController
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import spock.lang.Specification

@TestFor(NodeApiController)
@Mock([SecUser, SecUserSecRole, UnifinaCoreAPIFilters, SessionService])
class UnifinaCoreAPIFiltersSpec extends Specification {

	SecUser user
	SecRole adminRole, devopsRole
	SessionService sessionService

	void setup() {
		user = new SecUser().save(failOnError: true, validate: false)
		sessionService = Mock(SessionService)

		Key key = new Key(name: "k1", user: user)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		adminRole = new SecRole(authority: "ROLE_ADMIN").save(failOnError: true)
		devopsRole = new SecRole(authority: "ROLE_DEV_OPS").save(failOnError: true)
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
		new SecUserSecRole(secUser: user, secRole: adminRole).save(failOnError: true)

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
		given:
		defineBeans {
			sessionService(MethodInvokingFactoryBean) {
				targetObject = this
				targetMethod = "buildSessionServiceStub"
				arguments = [false]
			}
		}
		when:
		new SecUserSecRole(secUser: user, secRole: adminRole).save(failOnError: true)
		String token = "mytoken"
		request.addHeader("Authorization", "Bearer " + token)
		request.requestURI = "/api/v1/nodes"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
	}

	private SessionService buildSessionServiceStub(boolean ok) {
		SessionService result = Stub(SessionService)
		result.getUserFromToken("mytoken") >> user
		return result
	}
}
