package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.InvalidArgumentsException
import com.unifina.api.NotPermittedException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.service.PermissionService
import com.unifina.service.SignupCodeService
import com.unifina.signalpath.messaging.MockMailService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(PermissionApiController)
@Mock([Permission, Key, Stream, SecUser, Canvas])
class PermissionApiControllerSpec extends ControllerSpecification {
	def permissionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	// Canvas and Stream chosen because one has string id and one has long id
	Canvas canvasOwned, canvasShared, canvasRestricted, canvasPublic
	Stream streamOwned, streamShared, streamRestricted

	SecUser me, other
	Permission canvasPermission, streamPermission, canvasAnonPermission
	List<Permission> ownerPermissions

	def setup() {
		controller.permissionService = permissionService = Mock(PermissionService)
		controller.ethereumIntegrationKeyService = ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		controller.mailService = new MockMailService()
		controller.signupCodeService = new SignupCodeService()

		me = new SecUser(id: 1, username: "me@me.net").save(validate: false)
		other = new SecUser(id: 2, username: "other").save(validate: false)

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

		canvasPermission = new Permission(id: 1, user: me, clazz: Canvas.name, stringId: canvasShared.id, operation: Operation.SHARE).save(validate: false)
		streamPermission = new Permission(id: 2, user: me, clazz: Stream.name, longId: streamShared.id, operation: Operation.SHARE).save(validate: false)
		canvasAnonPermission = new Permission(id: 3, anonymous: true, clazz: Canvas.name, stringId: canvasPublic.id, operation: Operation.READ).save(validate: false)

		// read permission allows opening stream/canvas but not opening sharing-dialog for that stream/canvas
		new Permission(user: me, clazz: Canvas.name, stringId: canvasRestricted.id, operation: Operation.READ).save(validate: false)
		new Permission(user: me, clazz: Stream.name, longId: streamRestricted.id, operation: Operation.READ).save(validate: false)

		// returned from API, for resource owner, together with granted permissions
		ownerPermissions = [
			new Permission(id: null, user: me, operation: Operation.READ),
			new Permission(id: null, user: me, operation: Operation.WRITE),
			new Permission(id: null, user: me, operation: Operation.SHARE)
		]
    }

	void "validate setup"() {
		expect:
		Canvas.count() == 4
		Stream.count() == 3
		Permission.count() == 5
		SecUser.count() == 2
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
		response.json[0].operation == "share"
		// matching with _ instead of canvasOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
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
		response.json[0].operation == "share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [streamPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
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
		response.json.operation == "share"
		// matching with _ instead of canvasOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
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
		response.json.operation == "share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [streamPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
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
		thrown NotPermittedException
	}

	void "save grants Permissions"() {
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id

		when:
		request.JSON = [user: other.username, operation: "read"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.READ) >> new Permission(user: other, operation: Operation.READ)
		response.status == 201
		response.json.user == other.username
		response.json.operation == "read"
	}

	void "save sends an email if the user has no account yet"() {
		setup:
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		when:
		request.JSON = [anonymous: false, user: "test@tester.test", operation: "read"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		controller.mailService.mailSent
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.READ) >> new Permission(user: other, operation: Operation.READ)
	}

	void "save sends an email if the user has an account"() {
		setup:
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		when:
		request.JSON = [anonymous: false, user: "me@me.net", operation: "read"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		controller.mailService.mailSent
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.READ) >> new Permission(user: other, operation: Operation.READ)
	}

	void "save() creates a new user with permission if unknown ethereum address provided"() {
		setup:
		SecUser ethUser = new SecUser(id: 3, username: "0xa50E97f6a98dD992D9eCb8207c2Aa58F54970729")
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		when:
		request.JSON = [user: ethUser.username, operation: "read"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		!controller.mailService.mailSent
		1 * ethereumIntegrationKeyService.createEthereumUser(ethUser.username) >> ethUser
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.READ) >> new Permission(user: ethUser, operation: Operation.READ)
		response.status == 201
		response.json.user == ethUser.username
		response.json.operation == "read"
	}

	void "save sends an email for read permission"() {
		setup:
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		when:
		request.JSON = [anonymous: false, user: "me@me.net", operation: "read"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		controller.mailService.mailSent
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.READ) >> new Permission(user: other, operation: Operation.READ)
	}

	void "save does not send an email for write permission"() {
		setup:
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		when:
		request.JSON = [anonymous: false, user: "me@me.net", operation: "write"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		!controller.mailService.mailSent
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.WRITE) >> new Permission(user: other, operation: Operation.WRITE)
	}

	void "save does not send an email for share permission"() {
		setup:
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id
		when:
		request.JSON = [anonymous: false, user: "me@me.net", operation: "share"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		!controller.mailService.mailSent
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.SHARE) >> new Permission(user: other, operation: Operation.SHARE)
	}

	void "delete revokes permissions"() {
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		authenticatedAs(me) { controller.delete("${canvasPermission.id}") }
		then:
		response.status == 204
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService._
	}

	void "can't give both 'user' and 'anonymous' arguments"() {
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id

		when:
		request.JSON = [anonymous: true, user: other.username, operation: "read"] as JSON
		authenticatedAs(me) { controller.save() }
		then:
		thrown InvalidArgumentsException
	}

	void "getOwnPermissions giver owner permissions for own canvas"() {
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id

		when:
		authenticatedAs(me) { controller.getOwnPermissions() }
		then:
		response.status == 200
		response.json*.operation == ownerPermissions*.toMap()*.operation

		1 * permissionService.getPermissionsTo(_, me) >> [*ownerPermissions]
		0 * permissionService._
	}

	void "getOwnPermissions lists granted permissions for shared canvas"() {
		params.id = canvasShared.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		authenticatedAs(me) { controller.getOwnPermissions() }
		then:
		response.status == 200
		response.json == [[id: 1, operation: "share", user: "me@me.net"]]

		1 * permissionService.getPermissionsTo(_, me) >> [canvasPermission]
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
