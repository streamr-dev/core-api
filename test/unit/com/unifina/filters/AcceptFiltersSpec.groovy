package com.unifina.filters

import com.unifina.controller.api.PermissionApiController
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import spock.lang.Specification

@Mock([AcceptFilters, Permission, Key, Stream, SecUser, Canvas])
@TestFor(PermissionApiController)
@TestMixin(FiltersUnitTestMixin)
class AcceptFiltersSpec extends Specification {

	SecUser user
	Stream stream

    def setup() {
		controller.permissionService = Mock(PermissionService)
		user = new SecUser(username: "me@me.net").save(failOnError: true, validate: false)
		stream = new Stream()
		stream.id = "stream-id"
		stream.save(validate: false)
	}

    def cleanup() {
    }

    void "test something"() {
		setup:
		println(request.getHeader("Content-Type"))
		//request.setContentType("application/json")
		request.requestURI = "/api/v1/something"
		request.method = "POST"
		// set request.JSON without setting Content-Type header
		request.setContent(([
			anonymous: false,
			user: "username@email.com",
			operation: "stream_get",
		] as JSON).toString().getBytes("UTF-8"))
		params.resourceClass = Stream
		params.resourceId = stream.id

		println(request.getHeader("Content-Type"))
		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		//request.getHeader("Content-Type") == "application/json"
		1 * controller.permissionService.savePermissionAndSendEmailShareResourceInvite(_, _, _, _) >> new Permission(user: user, operation: Permission.Operation.STREAM_GET)
	}
}
