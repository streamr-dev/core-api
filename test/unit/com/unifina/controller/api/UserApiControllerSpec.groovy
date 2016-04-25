package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.service.UserService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import com.unifina.filters.UnifinaCoreAPIFilters
import spock.lang.Specification

@TestFor(UserApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, UnifinaCoreAPIFilters, UserService, SpringSecurityService])
class UserApiControllerSpec extends Specification {

	SecUser me

	def setup() {
		me = new SecUser(
			id: 1,
			name: "me",
			username: "me@too.com",
			apiKey: "myApiKey",
			enabled: true,
			timezone: "Europe/Helsinki"
		).save(validate: false)
	}

	void "unauthenticated user gets back 401"() {
		when:
		request.requestURI = "/api/v1/users/me"
		withFilters(action: "getUserInfo") { controller.getUserInfo() }
		then:
		response.status == 401
	}

	void "authenticated user gets back specified user info from /me"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/users/me"
		withFilters(action: "getUserInfo") { controller.getUserInfo() }
		then:
		response.json.name == me.name
		response.json.username == me.username
		response.json.apiKey == me.apiKey
		response.json.timezone == me.timezone
		!response.json.hasProperty("password")
		!response.json.hasProperty("id")
	}
}
