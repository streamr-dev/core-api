package com.unifina.controller.api

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.PermissionService
import com.unifina.service.UserService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

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

	void "save() creates user-linked for logged in user"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/users/me/keys"
		request.JSON = [
				name: "key name"
		]
		params.resourceClass = SecUser
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
		    id: "1",
			name: "key name",
			user: loggedInUser.username
		]
	}

	void "save() throws NotFoundException (404) if given resource does not exist"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		request.JSON = [
				name: "key name"
		]
		params.id = "streamId"
		params.resourceClass = Stream
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		thrown(NotFoundException)
	}

	void "save() throws AccessControlException if current user does not have share permission on given resource"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.WRITE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		request.JSON = [
				name: "key name"
		]
		params.id = stream.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		thrown(NotPermittedException)
	}

	void "save() creates anonymous key for resource when given id"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.SHARE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		request.JSON = [
				name: "key name",
				permission: "read"
		]
		params.id = stream.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
			id: "1",
			name: "key name",
			permission: "read",
			user: JSONObject.NULL
		]

		and:
		Permission.findAllByKey(Key.get(1)).size() == 1
		permissionService.canRead(Key.get(1), Stream.get(stream.id))
	}

	void "save() with write permission also creates read permission"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(loggedInUser, stream, Permission.Operation.SHARE)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "POST"
		request.requestURI = "/api/v1/streams/streamId/keys"
		request.JSON = [
				name: "key name",
				permission: "write"
		]
		params.id = stream.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		withFilters([action: 'save']) {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
				id: "1",
				name: "key name",
				permission: "write",
				user: JSONObject.NULL
		]

		and:
		Permission.findAllByKey(Key.get(1)).size() == 2
		permissionService.canRead(Key.get(1), Stream.get(stream.id))
		permissionService.canWrite(Key.get(1), Stream.get(stream.id))
	}

	void "delete() throws NotFoundException if given keyId doesn't exist"() {
		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me/keys/keyId"
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

		Key otherKey = new Key(name: "user2's key", user: user2).save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me/keys/${otherKey.id}"
		params.id = otherKey.id
		params.resourceClass = SecUser
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		thrown(NotPermittedException)
	}

	void "delete() deletes key if deleting user-linked key as said user"() {
		Key key = new Key(name: "user's key", user: loggedInUser).save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/keys/${key.id}"
		params.id = key.id
		params.resourceClass = SecUser
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		response.status == 204
		Key.get(key.id) == null
	}

	void "delete() does not permit deleting the user's only api key"() {
		expect:
		loggedInUser.keys.size() == 1

		when:
		def key = loggedInUser.keys.iterator().next()
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/users/me/keys/${key.id}"
		params.id = key.id
		params.resourceClass = SecUser
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		thrown(NotPermittedException)
	}

	void "delete() throws NotPermittedException if attempting to delete anonymous key that is not associated with a resource that logged in user has share permission on"() {
		setup:
		Stream stream = new Stream(name: "stream")
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		Key key = new Key(name: "anonymous key").save(failOnError: true, validate: true)

		controller.permissionService = permissionService = Stub(PermissionService)
		permissionService.get(Stream, loggedInUser, Permission.Operation.SHARE) >> [stream]
		permissionService.canRead(key, stream) >> false

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/streams/${stream.id}/keys/${key.id}"
		params.id = key.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		thrown(NotPermittedException)
	}

	void "delete() deletes anonymous key if it is associated with a resource that logged in user has share permission on"() {
		setup:
		Stream stream = new Stream(name: "stream")
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		Key key = new Key(name: "anonymous key").save(failOnError: true, validate: true)

		controller.permissionService = permissionService = Stub(PermissionService)
		permissionService.canShare(loggedInUser, stream) >> true

		assert Key.get(key.id) != null

		when:
		request.addHeader("Authorization", "Token apiKey")
		request.method = "DELETE"
		request.requestURI = "/api/v1/streams/${stream.id}/keys/${key.id}"
		params.id = key.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		withFilters([action: 'delete']) {
			controller.delete()
		}

		then:
		response.status == 204
		Key.get(key.id) == null
	}
}
