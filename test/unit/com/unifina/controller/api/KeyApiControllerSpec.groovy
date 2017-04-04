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

		def userLinkedKey = new Key(name: 'users key', user: loggedInUser)
		userLinkedKey.id = "apiKey"
		userLinkedKey.save(failOnError: true, validate: true)

		controller.permissionService = permissionService = grailsApplication.mainContext.getBean(PermissionService)
	}

	void "saveUserKey() creates user-linked for logged in user"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/keys"
		params.name = "key name"
		withFilters([action: 'saveUserKey']) {
			controller.saveUserKey()
		}

		then:
		response.status == 200
		response.json == [
		    id: "1",
			name: "key name",
			user: 1
		]
	}

	void "saveStreamKey() throws NotFoundException (404) if given streamId does not exist"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		params.id = "streamId"
		params.name = "key name"
		withFilters([action: 'saveStreamKey']) {
			controller.saveStreamKey()
		}

		then:
		thrown(NotFoundException)
	}

	void "saveStreamKey() throws AccessControlException if current user does not have share permission on given streamId"() {
		setup:
		Stream stream = new Stream()
		params.id = "streamId"
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.WRITE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		params.id = "streamId"
		params.name = "key name"
		withFilters([action: 'saveStreamKey']) {
			controller.saveStreamKey()
		}

		then:
		thrown(AccessControlException)
	}

	void "saveStreamKey() creates anonymous key for Stream when given streamId"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.SHARE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		params.id = "streamId"
		params.name = "key name"
		params.permission = "read"
		withFilters([action: 'saveStreamKey']) {
			controller.saveStreamKey()
		}

		then:
		response.status == 200
		response.json == [
			id: "1",
			name: "key name",
			user: JSONObject.NULL,
			permission: "read"
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
