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

	SecUser loggedInUser

	void setup() {
		loggedInUser = new SecUser(
			username: "user@user.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki"
		).save(failOnError: true, validate: true)
		controller.permissionService = permissionService = grailsApplication.mainContext.getBean(PermissionService)

		def key = new Key(name: 'users key', user: loggedInUser)
		key.id = "apiKey"
		key.save(failOnError: true, validate: true)
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

	void "save() throws NotPermittedException if not logged in as given username"() {
		setup:
		new SecUser(
			username: "user2@user.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki"
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
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/keys"
		request.JSON = [
			name: "key name",
			username: "user@user.com",
		]
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
		    id: "1",
			name: "key name",
			user: 1
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

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.WRITE)

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

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.SHARE)

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

	void "delete() throws NotFoundException if given keyId doesn't exist"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/keys/keyId"
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		thrown(NotFoundException)
	}

	void "delete() throws NotPermittedException if attempting to delete user-linked key as other user"() {
		setup:
		SecUser user2 = new SecUser(
			username: "user2@user.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki"
		).save(validate: true, failOnError: true)

		new Key(name: "user2's key", user: user2).save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/keys/1"
		params.id = "1"
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		thrown(NotPermittedException)
	}

	void "delete() deletes key if deleting user-linked key as said user"() {
		new Key(name: "user's key", user: loggedInUser).save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/keys/1"
		params.id = "1"
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		response.status == 204
		Key.get("1") == null
	}

	void "delete() throws NotPermittedException if attempting to delete anonymous key that is not associated with a Stream that logged in user has share permission on"() {
		setup:
		Stream stream = new Stream(name: "stream").save(validate: false, failOnError: true)
		Key key = new Key(name: "anonymous key").save(failOnError: true, validate: true)

		controller.permissionService = permissionService = Stub(PermissionService)
		permissionService.get(Stream, loggedInUser, Permission.Operation.SHARE) >> [stream]
		permissionService.canReadKey(key, stream) >> false

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/keys/1"
		params.id = "1"
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		thrown(NotPermittedException)
	}

	void "delete() deletes anonymous key if it is associated with a Stream that logged in user has share permission on"() {
		setup:
		Stream stream = new Stream(name: "stream").save(validate: false, failOnError: true)
		Key key = new Key(name: "anonymous key").save(failOnError: true, validate: true)

		controller.permissionService = permissionService = Stub(PermissionService)
		permissionService.get(Stream, loggedInUser, Permission.Operation.SHARE) >> [stream]
		permissionService.canReadKey(key, stream) >> true

		assert Key.get("1") != null

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/keys/1"
		params.id = "1"
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		response.status == 204
		Key.get("1") == null
	}
}
