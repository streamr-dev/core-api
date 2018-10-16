package com.unifina.controller.api

import com.unifina.FilterMockingSpecification
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

@TestFor(KeyApiController)
@Mock([Key, Permission, SecUser, Stream, UnifinaCoreAPIFilters, SpringSecurityService, UserService, PermissionService])
class KeyApiControllerSpec extends FilterMockingSpecification {

	def permissionService

	SecUser me

	def setup() {
		me = new SecUser(
			username: "me@me.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki"
		).save(failOnError: true, validate: true)

		def userLinkedKey = new Key(name: 'users key', user: me)
		userLinkedKey.id = "apiKey"
		userLinkedKey.save(failOnError: true, validate: true)

		controller.permissionService = permissionService = grailsApplication.mainContext.getBean(PermissionService)
	}

	// CORE-708: User with read permission to stream should not see stream write key in api
	void "index() does not authorize if only up to READ permission"() {
		Stream s = new Stream(name: "stream")
		s.id = "streamId"
		s.save(failOnError: true, validate: false)
		permissionService.systemGrant(me, s, Permission.Operation.READ)

		when:
		params.resourceClass = Stream
		params.resourceId = "streamId"

		authenticatedAs(me) { controller.index() }

		then:
		thrown(NotPermittedException)
	}

	void "save() creates user-linked for logged in user"() {
		when:
		request.method = "POST"
		request.JSON = [
				name: "key name"
		]
		params.resourceClass = SecUser
		authenticatedAs(me) { controller.save() }

		then:
		response.status == 200
		response.json == [
		    id: "1",
			name: "key name",
			user: me.username
		]
	}

	void "save() throws NotFoundException (404) if given resource does not exist"() {
		when:
		request.method = "POST"
		request.JSON = [
				name: "key name"
		]
		params.id = "streamId"
		params.resourceClass = Stream
		authenticatedAs(me) { controller.save() }

		then:
		thrown(NotFoundException)
	}

	void "save() throws AccessControlException if current user does not have share permission on given resource"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(me, stream, Permission.Operation.WRITE)

		when:
		request.method = "POST"
		request.JSON = [
				name: "key name"
		]
		params.id = stream.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		authenticatedAs(me) { controller.save() }

		then:
		thrown(NotPermittedException)
	}

	void "save() creates anonymous key for resource when given id"() {
		setup:
		Stream stream = new Stream()
		stream.id = "streamId"
		stream.save(validate: false, failOnError: true)

		permissionService.systemGrant(me, stream, Permission.Operation.SHARE)

		when:
		request.method = "POST"
		request.JSON = [
				name: "key name",
				permission: "read"
		]
		params.id = stream.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		authenticatedAs(me) { controller.save() }

		then:
		response.status == 200
		response.json == [
			id: "1",
			name: "key name",
			permission: "read",
			user: null
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

		permissionService.systemGrant(me, stream, Permission.Operation.SHARE)

		when:
		request.method = "POST"
		request.JSON = [
				name: "key name",
				permission: "write"
		]
		params.id = stream.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		authenticatedAs(me) { controller.save() }

		then:
		response.status == 200
		response.json == [
				id: "1",
				name: "key name",
				permission: "write",
				user: null
		]

		and:
		Permission.findAllByKey(Key.get(1)).size() == 2
		permissionService.canRead(Key.get(1), Stream.get(stream.id))
		permissionService.canWrite(Key.get(1), Stream.get(stream.id))
	}

	void "delete() throws NotFoundException if given keyId doesn't exist"() {
		when:
		request.method = "DELETE"
		authenticatedAs(me) { controller.delete() }

		then:
		thrown(NotFoundException)
	}

	void "delete() throws NotPermittedException if attempting to delete user-linked key as other user"() {
		setup:
		SecUser user2 = new SecUser(
			username: "user2@me.com",
			password: "pwd",
			name: "name",
			timezone: "Europe/Helsinki"
		).save(validate: true, failOnError: true)

		Key otherKey = new Key(name: "user2's key", user: user2).save(failOnError: true, validate: true)

		when:
		request.method = "DELETE"
		params.id = otherKey.id
		params.resourceClass = SecUser
		authenticatedAs(me) { controller.delete() }

		then:
		thrown(NotPermittedException)
	}

	void "delete() deletes key if deleting user-linked key as said user"() {
		Key key = new Key(name: "me's key", user: me).save(failOnError: true, validate: true)

		when:
		request.method = "DELETE"
		params.id = key.id
		params.resourceClass = SecUser
		authenticatedAs(me) { controller.delete() }

		then:
		response.status == 204
		Key.get(key.id) == null
	}

	void "delete() does not permit deleting the user's only api key"() {
		expect:
		me.keys.size() == 1

		when:
		def key = me.keys.iterator().next()
		request.method = "DELETE"
		params.id = key.id
		params.resourceClass = SecUser
		authenticatedAs(me) { controller.delete() }

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
		permissionService.get(Stream, me, Permission.Operation.SHARE) >> [stream]
		permissionService.canRead(key, stream) >> false

		when:
		request.method = "DELETE"
		params.id = key.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		authenticatedAs(me) { controller.delete() }

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
		permissionService.canShare(me, stream) >> true

		assert Key.get(key.id) != null

		when:
		request.method = "DELETE"
		params.id = key.id
		params.resourceClass = Stream
		params.resourceId = stream.id
		authenticatedAs(me) { controller.delete() }

		then:
		response.status == 204
		Key.get(key.id) == null
	}
}
