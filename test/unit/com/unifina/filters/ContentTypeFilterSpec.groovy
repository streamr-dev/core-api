package com.unifina.filters

import com.unifina.BeanMockingSpecification
import com.unifina.controller.api.UnitTestController
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin

@TestFor(UnitTestController)
@Mock([SecUser, UnifinaCoreAPIFilters])
@TestMixin(FiltersUnitTestMixin)
class ContentTypeFilterSpec extends BeanMockingSpecification  {
	SecUser user
	Key key
	SessionService sessionService

	def setup() {
		user = new SecUser().save(failOnError: true, validate: false)

		key = new Key(name: "key", user: user)
		key.id = "apikey"
		key.save(failOnError: true, validate: true)

		sessionService = mockBean(SessionService, Mock(SessionService))
	}

	void "contentTypeFilter POST accepts only StreamrApi#expectedContentTypes"() {
		setup:
		request.addHeader("Authorization", "Token " + key.id)
		request.method = "POST"
		request.requestURI = "/api/v1/foo"
		request.setContentType("text/plain")
		byte[] data = "data to send".getBytes("UTF-8")
		request.setContent(data)
		request.addHeader("Content-Length", data.length)
		when:
		withFilters(action: "upload") {
			controller.upload()
		}
		then:
		response.status == 415
	}

	void "contentTypeFilter POST accepts correct content type"() {
		setup:
		request.addHeader("Authorization", "Token " + key.id)
		request.method = "POST"
		request.requestURI = "/api/v1/foo"
		request.setContentType("text/csv")
		byte[] data = "data to send".getBytes("UTF-8")
		request.setContent(data)
		request.addHeader("Content-Length", data.length)
		when:
		withFilters(action: "upload") {
			controller.upload()
		}
		then:
		response.status == 200
	}

	void "contentTypeFilter GET ignores content type"() {
		setup:
		request.addHeader("Authorization", "Token " + key.id)
		request.method = "GET"
		request.requestURI = "/api/v1/foo"
		request.setContentType("text/plain")
		byte[] data = "data to send".getBytes("UTF-8")
		request.setContent(data)
		request.addHeader("Content-Length", data.length)
		when:
		withFilters(action: "upload") {
			controller.upload()
		}
		then:
		response.status == 200
	}}

