package com.unifina.service

import com.unifina.controller.StreamListParams
import com.unifina.domain.Permission
import com.unifina.domain.Stream
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(ApiService)
@Mock(Stream)
class ApiServiceSpec extends Specification {

	void "list() returns streams with share permission"() {
		def permissionService = service.permissionService = Mock(PermissionService)
		ListParams listParams = new StreamListParams(operation: Permission.Operation.STREAM_SHARE.toString(), publicAccess: true)
		User me = new User(username: "me@me.com")

		when:
		def list = service.list(Stream, listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(Stream, me, Permission.Operation.STREAM_SHARE, true, _) >> [
			new Stream(), new Stream(), new Stream()
		]
	}

	void "list() delegates to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "me@me.com")
		ListParams listParams = new StreamListParams(publicAccess: true)

		when:
		def list = service.list(Stream, listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(Stream, me, Permission.Operation.STREAM_GET, true, _) >> [
			new Stream(), new Stream(), new Stream()
		]
	}

	void "list() passes user as null to permissionService#get if grantedAccess=false"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "me@me.com")
		ListParams listParams = new StreamListParams(publicAccess: true, grantedAccess: false)

		when:
		service.list(Stream, listParams, me)

		then:
		1 * permissionService.get(Stream, null, _, _, _)
	}

	void "list() invokes listParams#validate and listParams#createListCriteria and passes returned closure to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "me@me.com")
		ListParams listParams = Mock(ListParams)

		when:
		service.list(Stream, listParams, me)

		then:
		1 * permissionService.get(_, _, _, _, { it() == "see me?" })
		1 * listParams.validate() >> true
		1 * listParams.createListCriteria() >> { { a -> "see me?" } }
	}

	void "list() throws ValidationException if validation of listParams fails"() {
		User me = new User(username: "me@me.com")
		ListParams listParams = new StreamListParams(order: null)

		when:
		service.list(Stream, listParams, me)
		then:
		thrown(ValidationException)
	}

	void "getByIdAndThrowIfNotFound() throws NotFoundException if domain object cannot be found"() {
		when:
		service.getByIdAndThrowIfNotFound(Stream, "stream-id")

		then:
		def e = thrown(NotFoundException)
		e.asApiError().toMap() == [
			id: "stream-id",
			message: "Stream with id stream-id not found",
			code: "NOT_FOUND",
			fault: "id",
			type: "Stream"
		]
	}

	void "getByIdAndThrowIfNotFound() returns domain object if it exists"() {
		Stream stream = new Stream(name: "stream")
		stream.id = "stream-id"
		stream.save(failOnError: true)

		expect:
		service.getByIdAndThrowIfNotFound(Stream, "stream-id") == stream
	}

	void "authorizedGetById() throws NotFoundException if domain object cannot be found"() {
		User me = new User(username: "me@me.com")

		when:
		service.authorizedGetById(Stream, "stream-id", me, Permission.Operation.STREAM_EDIT)

		then:
		def e = thrown(NotFoundException)
		e.asApiError().toMap() == [
			id: "stream-id",
			message: "Stream with id stream-id not found",
			code: "NOT_FOUND",
			fault: "id",
			type: "Stream"
		]
	}

	void "authorizedGetById() throws NotPermittedException if user does not have required permission"() {
		User me = new User(username: "me@me.com")
		Stream stream = new Stream(name: "stream")
		stream.id = "stream-id"
		stream.save(failOnError: true)

		service.permissionService = new PermissionService()

		when:
		service.authorizedGetById(Stream, "stream-id", me, Permission.Operation.STREAM_EDIT)

		then:
		def e = thrown(NotPermittedException)
		e.message == "me@me.com does not have permission to stream_edit Stream (id stream-id)"
	}

	void "authorizedGetById() returns domain object if it exists and user has required permission"() {
		User me = new User(username: "me@me.com")
		Stream stream = new Stream(name: "stream")
		stream.id = "stream-id"
		stream.save(failOnError: true)

		service.permissionService = Stub(PermissionService) // replace verify() with nop method

		when:
		def result = service.authorizedGetById(Stream, "stream-id", me, Permission.Operation.STREAM_EDIT)

		then:
		result == stream
	}
}
