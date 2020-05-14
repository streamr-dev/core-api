package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.Resource
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.mail.MailService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(PermissionApiController)
@Mock([Permission, Key, Stream, SecUser, Canvas])
class PermissionApiControllerSpec extends ControllerSpecification {
	def permissionService
	StreamService streamService

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
		controller.streamService = streamService = Mock(StreamService)

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
		params.id = canvasShared.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		authenticatedAs(me) { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == canvasPermission.id
		response.json[0].user == "me@me.net"
		response.json[0].operation == "canvas_share"
		// matching with _ instead of canvasOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_, true, null) >> [canvasPermission, *ownerPermissions]
		1 * permissionService.check(me, _, Permission.Operation.CANVAS_SHARE) >> true
		0 * permissionService._
    }

	void "index returns list of permissions to shared resource (Stream using id)"() {
		params.id = streamShared.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id

		when:
		authenticatedAs(me) { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == streamPermission.id
		response.json[0].user == "me@me.net"
		response.json[0].operation == "stream_share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * streamService.getStream(streamShared.id) >> streamShared
		1 * permissionService.getPermissionsTo(_, true, null) >> [streamPermission, *ownerPermissions]
		1 * permissionService.check(me, _, Permission.Operation.STREAM_SHARE) >> true
		0 * permissionService._
	}

	void "index returns list of permissions filtered by subscription"() {
		params.id = streamShared.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id
		params.subscriptions = "false"

		when:
		authenticatedAs(me) { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == streamPermission.id
		response.json[0].user == "me@me.net"
		response.json[0].operation == "stream_share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * streamService.getStream(streamShared.id) >> streamShared
		1 * permissionService.getPermissionsTo(_, false, null) >> [streamPermission, *ownerPermissions]
		1 * permissionService.check(me, _, Permission.Operation.STREAM_SHARE) >> true
		0 * permissionService._
	}

	void "show returns one specific permission row to shared resource (string id)"() {
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		authenticatedAs(me) { controller.show("${canvasPermission.id}") }
		then:
		response.status == 200
		response.json.id == 1
		response.json.user == "me@me.net"
		response.json.operation == "canvas_share"
		// matching with _ instead of canvasOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService.check(me, _, Permission.Operation.CANVAS_SHARE) >> true
		0 * permissionService._
	}

	void "show returns one specific permission row to shared resource (Stream using id)"() {
		params.id = streamPermission.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id

		when:
		authenticatedAs(me) { controller.show("${streamPermission.id}") }
		then:
		response.status == 200
		response.json.id == 2
		response.json.user == "me@me.net"
		response.json.operation == "stream_share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * streamService.getStream(streamShared.id) >> streamShared
		1 * permissionService.getPermissionsTo(_) >> [streamPermission, *ownerPermissions]
		1 * permissionService.check(me, _, Permission.Operation.STREAM_SHARE) >> true
		0 * permissionService._
	}

	void "index won't show list of permissions without 'share' permission (string id)"() {
		params.id = canvasRestricted.id
		params.resourceClass = Canvas
		params.resourceId = canvasRestricted.id

		when:
		authenticatedAs(me) { controller.index() }
		then:
		thrown NotPermittedException
	}

	void "index won't show list of permissions without 'share' permission (Stream using id)"() {
		params.id = streamRestricted.id
		params.resourceClass = Stream
		params.resourceId = streamRestricted.id

		when:
		authenticatedAs(me) { controller.index() }
		then:
		1 * streamService.getStream(streamRestricted.id) >> streamRestricted
		thrown NotPermittedException
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
		response.header("Location") == request.forwardURI + "/" + newPermission.id
		response.status == 201
		response.json.user == me.username
		response.json.operation == "canvas_get"
	}

	void "delete revokes permissions"() {
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		authenticatedAs(me) { controller.delete("${canvasPermission.id}") }
		then:
		response.status == 204
		1 * permissionService.check(me, _, Operation.CANVAS_SHARE) >> true
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService._
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

	void "getOwnPermissions giver owner permissions for own canvas"() {
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
		response.status == 200
	}
}
