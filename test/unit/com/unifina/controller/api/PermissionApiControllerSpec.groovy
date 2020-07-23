package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.ValidationException
import com.unifina.domain.Resource
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.mail.MailService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(PermissionApiController)
@Mock([Permission, Key, Stream, SecUser, Canvas])
class PermissionApiControllerSpec extends ControllerSpecification {
	def permissionService

	// Canvas and Stream chosen because one has string id and one has long id
	Canvas canvasOwned, canvasShared, canvasRestricted, canvasPublic
	Stream streamOwned, streamShared, streamRestricted

	SecUser me, other
	Permission canvasPermission, streamPermission, canvasAnonPermission
	List<Permission> ownerPermissions

	def setup() {
		controller.permissionService = Mock(PermissionService)
		controller.permissionService.mailService = Mock(MailService)
		controller.permissionService.groovyPageRenderer = Mock(PageRenderer)

		controller.permissionService = permissionService = Mock(PermissionService)

		me = new SecUser(id: 1, username: "me@me.net").save(validate: false)
		other = new SecUser(id: 2, username: "0x0000000000000000000000000000000000000000").save(validate: false)

		def meKey = new Key(name: "meKey", user: me)
		meKey.id = "myApiKey"
		meKey.save(failOnError: true, validate: true)

		def otherKey = new Key(name: "otherKey", user: me)
		otherKey.id = "otherApiKey"
		otherKey.save(failOnError: true, validate: true)

		def newCanvas = { String id ->
			def c = new Canvas()
			c.id = id
			return c.save(validate: false)
		}
		canvasOwned = newCanvas("own")
		canvasShared = newCanvas("shared")
		canvasRestricted = newCanvas("restricted")
		canvasPublic = newCanvas("public")

		def newStream = { String id, SecUser owner ->
			def c = new Stream()
			c.id = id
			return c.save(validate: false)
		}
		streamOwned = newStream("own", me)
		streamShared = newStream("shared", other)
		streamRestricted = newStream("restricted", other)

		canvasPermission = new Permission(id: 1, user: me, clazz: Canvas.name, stringId: canvasShared.id, operation: Operation.CANVAS_SHARE).save(validate: false)
		streamPermission = new Permission(id: 2, user: me, clazz: Stream.name, longId: streamShared.id, operation: Operation.STREAM_SHARE).save(validate: false)
		canvasAnonPermission = new Permission(id: 3, anonymous: true, clazz: Canvas.name, stringId: canvasPublic.id, operation: Operation.CANVAS_GET).save(validate: false)

		// read permission allows opening stream/canvas but not opening sharing-dialog for that stream/canvas
		new Permission(user: me, clazz: Canvas.name, stringId: canvasRestricted.id, operation: Operation.CANVAS_GET).save(validate: false)
		new Permission(user: me, clazz: Stream.name, longId: streamRestricted.id, operation: Operation.CANVAS_GET).save(validate: false)

		// returned from API, for resource owner, together with granted permissions
		ownerPermissions = [
			new Permission(id: null, user: me, operation: Operation.CANVAS_GET),
			new Permission(id: null, user: me, operation: Operation.CANVAS_EDIT),
			new Permission(id: null, user: me, operation: Operation.CANVAS_SHARE)
		]
    }

    void "index returns list of permissions to shared resource (string id)"() {
		setup:
		params.id = canvasShared.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == canvasPermission.id
		response.json[0].user == "me@me.net"
		response.json[0].operation == "canvas_share"
		1 * permissionService.findAllPermissions(resource, request.apiUser, null, true) >> [canvasPermission, *ownerPermissions]
		0 * permissionService._
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
		response.json[0].user == "me@me.net"
		response.json[0].operation == "stream_share"
		1 * permissionService.findAllPermissions(resource, request.apiUser, null, true) >> [streamPermission, *ownerPermissions]
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
		response.json[0].user == "me@me.net"
		response.json[0].operation == "stream_share"
		1 * permissionService.findAllPermissions(resource, request.apiUser, null,false) >> [streamPermission, *ownerPermissions]
		0 * permissionService._
	}

	void "show returns one specific permission row to shared resource (string id)"() {
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.show() }
		then:
		response.status == 200
		response.json.user == "me@me.net"
		response.json.operation == "canvas_share"
		1 * permissionService.findPermission(canvasPermission.id, resource, request.apiUser, null) >> canvasPermission
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
		response.json.id == 2
		response.json.user == "me@me.net"
		response.json.operation == "stream_share"
		1 * permissionService.findPermission(streamPermission.id, resource, request.apiUser, null) >> streamPermission
		0 * permissionService._
	}

	void "save rejects invalid email username or invalid ethereum address username"() {
		setup:
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		request.JSON = [
			user: "invalid-email-or-ethereum-address",
			operation: "canvas_get",
		] as JSON
		when:
		authenticatedAs(me) { controller.save() }
		then:
		def e = thrown(ValidationException)
	}

	void "save grants Permissions"() {
		setup:
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		controller.permissionService = Mock(PermissionService)
		Permission newPermission = new Permission(user: me, operation: Operation.CANVAS_GET, canvas: canvasOwned)
		newPermission.save()
		when:
		request.JSON = [
			anonymous: false,
			user: "me@me.net",
			operation: "canvas_get",
		] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		1 * controller.permissionService.savePermissionAndSendShareResourceEmail(
			_,
			_,
			Permission.Operation.CANVAS_GET,
			_,
			_
		) >> newPermission
		0 * permissionService._
		response.header("Location") == request.forwardURI + "/" + newPermission.id
		response.status == 200
		response.json.user == me.username
		response.json.operation == "canvas_get"
	}

	void "delete revokes permissions"() {
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		request.apiUser = me

		when:
		authenticatedAs(me) { controller.delete() }
		then:
		response.status == 204
		1 * permissionService.deletePermission(_, resource, request.apiUser, null)
		0 * permissionService._
	}

	void "can't give both 'user' and 'anonymous' arguments"() {
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id

		when:
		request.JSON = [anonymous: true, user: other.username, operation: "canvas_get"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		thrown ValidationException
	}

	void "getOwnPermissions gets owner permissions for own canvas"() {
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		request.apiUser = me
		Resource resource = new Resource(params.resourceClass, params.resourceId)

		when:
		authenticatedAs(me) { controller.getOwnPermissions() }
		then:
		response.status == 200
		response.json*.operation == ownerPermissions*.toMap()*.operation
		1 * permissionService.getOwnPermissions(resource, me, null) >> [*ownerPermissions]
		0 * permissionService._
	}

	void "getOwnPermissions lists granted permissions for shared canvas"() {
		params.id = canvasShared.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id
		request.apiUser = me
		Resource resource = new Resource(params.resourceClass, params.resourceId)

		when:
		authenticatedAs(me) { controller.getOwnPermissions() }
		then:
		response.status == 200
		response.json == [[id: 1, operation: "canvas_share", user: "me@me.net"]]
		1 * permissionService.getOwnPermissions(resource, me, null) >> [canvasPermission]
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
