package com.unifina.controller


import com.unifina.domain.Permission
import com.unifina.domain.Permission.Operation
import com.unifina.domain.Resource
import com.unifina.domain.Stream
import com.unifina.domain.User
import com.unifina.service.PermissionService
import com.unifina.service.ValidationException
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.mail.MailService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(PermissionApiController)
@Mock([Permission, Stream, User])
class PermissionApiControllerSpec extends ControllerSpecification {
	PermissionService permissionService
	Stream streamPublic
	Stream streamOwned
	Stream streamShared
	Stream streamRestricted
	User me
	User other
	Permission streamPermission
	Permission streamAnonPermission
	List<Permission> ownerPermissions

	def setup() {
		controller.permissionService = Mock(PermissionService)
		controller.permissionService.mailService = Mock(MailService)
		controller.permissionService.groovyPageRenderer = Mock(PageRenderer)
		controller.permissionService = permissionService = Mock(PermissionService)

		me = new User(id: 1, username: "0x0000000000000000000000000000000000000001").save(validate: false)
		other = new User(id: 2, username: "0x0000000000000000000000000000000000000000").save(validate: false)

		def newStream = { String id ->
			def c = new Stream(name: "Stream " + id)
			c.id = id
			return c.save(validate: false)
		}
		streamOwned = newStream("own")
		streamShared = newStream("shared")
		streamRestricted = newStream("restricted")
		streamPublic = newStream("public")

		streamPermission = new Permission(id: 2, user: me, clazz: Stream.name, longId: streamShared.id, operation: Operation.STREAM_SHARE).save(validate: false)
		streamAnonPermission = new Permission(id: 3, anonymous: true, clazz: Stream.name, stringId: streamPublic.id, operation: Operation.STREAM_GET).save(validate: false)

		// read permission allows opening stream but not opening sharing-dialog for that stream
		new Permission(user: me, clazz: Stream.name, longId: streamRestricted.id, operation: Operation.STREAM_GET).save(validate: false)

		// returned from API, for resource owner, together with granted permissions
		ownerPermissions = [
			new Permission(id: null, user: me, operation: Operation.STREAM_GET),
			new Permission(id: null, user: me, operation: Operation.STREAM_EDIT),
			new Permission(id: null, user: me, operation: Operation.STREAM_SHARE)
		]
	}

	void "index returns list of permissions to shared resource (Stream using id)"() {
		setup:
		params.id = streamShared.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == streamPermission.id
		response.json[0].user == "0x0000000000000000000000000000000000000001"
		response.json[0].operation == "stream_share"
		1 * permissionService.findAllPermissions(resource, request.apiUser, true) >> [streamPermission, *ownerPermissions]
		0 * permissionService._
	}

	void "index returns list of permissions filtered by subscription"() {
		setup:
		params.id = streamShared.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id
		params.subscriptions = "false"
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == streamPermission.id
		response.json[0].user == "0x0000000000000000000000000000000000000001"
		response.json[0].operation == "stream_share"
		1 * permissionService.findAllPermissions(resource, request.apiUser, false) >> [streamPermission, *ownerPermissions]
		0 * permissionService._
	}

	void "show returns one specific permission row to shared resource (Stream using id)"() {
		params.id = streamPermission.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.show() }
		then:
		response.status == 200
		response.json.id == 1
		response.json.user == "0x0000000000000000000000000000000000000001"
		response.json.operation == "stream_share"
		1 * permissionService.findPermission(streamPermission.id, resource, request.apiUser) >> streamPermission
		0 * permissionService._
	}

	void "save rejects invalid email username or invalid ethereum address username"() {
		setup:
		params.id = streamOwned.id
		params.resourceClass = Stream
		params.resourceId = streamOwned.id
		request.JSON = [
			user: "invalid-email-or-ethereum-address",
			operation: "stream_get",
		] as JSON
		when:
		authenticatedAs(me) { controller.save() }
		then:
		def e = thrown(ValidationException)
	}

	void "save grants Permissions"() {
		setup:
		params.resourceClass = Stream
		params.resourceId = streamOwned.id
		controller.permissionService = Mock(PermissionService)
		Permission newPermission = new Permission(user: me, operation: Operation.STREAM_GET, stream: streamOwned)
		newPermission.save()
		when:
		request.JSON = [
			anonymous: false,
			user: "0x0000000000000000000000000000000000000001",
			operation: "stream_get",
		] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		1 * controller.permissionService.savePermissionAndSendShareResourceEmail(
			_,
			Permission.Operation.STREAM_GET,
			_,
			_
		) >> newPermission
		0 * permissionService._
		response.header("Location") == request.forwardURI + "/" + newPermission.id
		response.status == 200
		response.json.user == me.username
		response.json.operation == "stream_get"
	}

	void "delete revokes permissions"() {
		params.id = streamPermission.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.delete() }
		then:
		response.status == 204
		1 * permissionService.deletePermission(_, resource, request.apiUser)
		0 * permissionService._
	}

	void "can't give both 'user' and 'anonymous' arguments"() {
		params.id = streamOwned.id
		params.resourceClass = Stream
		params.resourceId = streamOwned.id

		when:
		request.JSON = [anonymous: true, user: other.username, operation: "stream_get"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		thrown ValidationException
	}

	void "getOwnPermissions gets owner permissions for own stream"() {
		params.id = streamOwned.id
		params.resourceClass = Stream
		params.resourceId = streamOwned.id
		request.apiUser = me
		Resource resource = new Resource(params.resourceClass, params.resourceId)

		when:
		authenticatedAs(me) { controller.getOwnPermissions() }
		then:
		response.status == 200
		response.json*.operation == ownerPermissions*.toMap()*.operation
		1 * permissionService.getOwnPermissions(resource, me) >> [*ownerPermissions]
		0 * permissionService._
	}

	void "getOwnPermissions lists granted permissions for shared stream"() {
		params.id = streamShared.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id
		request.apiUser = me
		Resource resource = new Resource(params.resourceClass, params.resourceId)

		when:
		authenticatedAs(me) { controller.getOwnPermissions() }
		then:
		response.status == 200
		response.json == [[id: 1, operation: "stream_share", user: "0x0000000000000000000000000000000000000001"]]
		1 * permissionService.getOwnPermissions(resource, me) >> [streamPermission]
		0 * permissionService._
	}

	void "cleanup calls service to delete expired permissions"() {
		when:
		request.method = "DELETE"
		authenticatedAs(me) { controller.cleanup() }

		then:
		1 * permissionService.cleanUpExpiredPermissions()
		0 * permissionService._
		response.status == 200
	}
}