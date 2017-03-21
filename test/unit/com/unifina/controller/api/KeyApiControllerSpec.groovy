package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import com.unifina.service.UserService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

import java.security.AccessControlException

@TestFor(KeyApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([Key, Permission, SecUser, Stream, UnifinaCoreAPIFilters, SpringSecurityService, UserService, PermissionService])
class KeyApiControllerSpec extends Specification {

	def permissionService

	void setup() {
		SecUser secUser = new SecUser(
			username: "user@user.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki",
			apiKey: "apiKey"
		)
		secUser.save(failOnError: true, validate: true)
		controller.permissionService = permissionService = grailsApplication.mainContext.getBean(PermissionService)
	}

	void "save() throws ApiException (status code 422) if not given either username or streamId"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		def e = thrown(ApiException)
		e.statusCode == 422
	}

	void "save() throws ApiException (status code 422) if given both username and streamId"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			username: "username",
			streamId: "streamId"
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		def e = thrown(ApiException)
		e.statusCode == 422
	}

	void "save() throws NotFoundException (404) if given username does not exist"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			username: "user2@user.com",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		thrown(NotFoundException)
	}

	void "save() throws NotPermittedException if current user does not have share permission on target user"() {
		setup:
		new SecUser(
			username: "user2@user.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki",
			apiKey: "apiKey"
		).save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			username: "user2@user.com",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		thrown(NotPermittedException)
	}

	void "save() creates user-linked key when given username"() {
		setup:
		new SecUser(
			username: "user2@user.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki",
			apiKey: "apiKey"
		).save(failOnError: true, validate: true)

		permissionService.systemGrant(SecUser.get(1), SecUser.get(2), Permission.Operation.SHARE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			username: "user2@user.com",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
		    id: "1",
			name: "key name",
			user: 2
		]
	}

	void "save() throws NotFoundException (404) if given streamId does not exist"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			streamId: "streamId",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		thrown(NotFoundException)
	}

	void "save() throws AccessControlException if current user does not have share permission on given streamId"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(SecUser.get(1), stream, Permission.Operation.WRITE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			streamId: "streamId",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		thrown(AccessControlException)
	}

	void "save() creates anonymous key for Stream when given streamId"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(SecUser.get(1), stream, Permission.Operation.SHARE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			streamId: "streamId",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
			id: "1",
			name: "key name",
			user: JSONObject.NULL
		]

		and:
		Permission.findByKey(Key.get(1)) != null
		permissionService.canReadKey(Key.get(1), Stream.get("streamId"))
	}
}
