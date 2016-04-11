package com.unifina.controller.api

import com.unifina.api.NotPermittedException
import com.unifina.api.InvalidArgumentsException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import com.unifina.service.UserService
import com.unifina.filters.UnifinaCoreAPIFilters
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import spock.lang.Specification

@TestFor(PermissionApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([Permission, Stream, SecUser, Canvas, UnifinaCoreAPIFilters, UserService])
class PermissionApiControllerSpec extends Specification {
	def permissionService

	// Canvas and Stream chosen because one has string id and one has long id
	Canvas canvasOwned, canvasShared, canvasRestricted
	Stream streamOwned, streamShared, streamRestricted

	SecUser me, other
	Permission canvasPermission, streamPermission

	def setup() {
		controller.permissionService = permissionService = Mock(PermissionService)

		me = new SecUser(id: 1, username: "me", apiKey: "myApiKey").save(validate: false)
		other = new SecUser(id: 2, username: "other", apiKey: "otherApiKey").save(validate: false)

		def newCanvas = { String id, SecUser owner ->
			def c = new Canvas(user: owner)
			c.id = id
			return c.save(validate: false)
		}
		canvasOwned = newCanvas("own", me)
		canvasShared = newCanvas("shared", other)
		canvasRestricted = newCanvas("restricted", other)

		def newStream = { String id, SecUser owner ->
			def c = new Stream(user: owner)
			c.id = id
			return c.save(validate: false)
		}
		streamOwned = newStream("own", me)
		streamShared = newStream("shared", other)
		streamRestricted = newStream("restricted", other)

		canvasPermission = new Permission(id: 1, user: me, clazz: Canvas.name, stringId: canvasShared.id, operation: Operation.SHARE).save(validate: false)
		streamPermission = new Permission(id: 2, user: me, clazz: Stream.name, longId: streamShared.id, operation: Operation.SHARE).save(validate: false)

		// read permission allows opening stream/canvas but not opening sharing-dialog for that stream/canvas
		new Permission(user: me, clazz: Canvas.name, stringId: canvasRestricted.id, operation: Operation.READ).save(validate: false)
		new Permission(user: me, clazz: Stream.name, longId: streamRestricted.id, operation: Operation.READ).save(validate: false)
    }

	// returned from API, for resource owner, together with granted permissions
	private def ownerPermissions = [
		new Permission(id: null, user: me, operation: "read"),
		new Permission(id: null, user: me, operation: "write"),
		new Permission(id: null, user: me, operation: "share")
	]

	void "validate setup"() {
		expect:
		Canvas.count() == 3
		Stream.count() == 3
		Permission.count() == 4
		SecUser.count() == 2
	}

    void "index returns list of permissions to shared resource (string id)"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/${canvasShared.id}/permissions"
		params.id = canvasShared.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		withFilters(action: "index") { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == canvasPermission.id
		response.json[0].user == "me"
		response.json[0].operation == "share"
		// matching with _ instead of canvasOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
		0 * permissionService._
    }

	void "index returns list of permissions to shared resource (Stream using id)"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/streams/${streamShared.id}/permissions"
		params.id = streamShared.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id

		when:
		withFilters(action: "index") { controller.index() }
		then:
		response.status == 200
		response.json.size() == 4
		response.json[0].id == streamPermission.id
		response.json[0].user == "me"
		response.json[0].operation == "share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [streamPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
		0 * permissionService._
	}

	void "show returns one specific permission row to shared resource (string id)"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/${canvasShared.id}/permissions/${canvasPermission.id}"
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		withFilters(action: "show") { controller.show("${canvasPermission.id}") }
		then:
		response.status == 200
		response.json.id == 1
		response.json.user == "me"
		response.json.operation == "share"
		// matching with _ instead of canvasOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
		0 * permissionService._
	}

	void "show returns one specific permission row to shared resource (Stream using id)"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/streams/${streamShared.id}/permissions/${streamPermission.id}"
		params.id = streamPermission.id
		params.resourceClass = Stream
		params.resourceId = streamShared.id

		when:
		withFilters(action: "show") { controller.show("${streamPermission.id}") }
		then:
		response.status == 200
		response.json.id == 2
		response.json.user == "me"
		response.json.operation == "share"
		// matching with _ instead of streamOwned because it's not "the same" after saving and get(id):ing
		1 * permissionService.getPermissionsTo(_) >> [streamPermission, *ownerPermissions]
		1 * permissionService.canShare(me, _) >> true
		0 * permissionService._
	}

	void "index won't show list of permissions without 'share' permission (string id)"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/allowed/permissions"
		params.id = canvasRestricted.id
		params.resourceClass = Canvas
		params.resourceId = canvasRestricted.id

		when:
		withFilters(action: "index") { controller.index() }
		then:
		thrown NotPermittedException
	}

	void "index won't show list of permissions without 'share' permission (Stream using id)"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/streams/${streamRestricted.id}/permissions"
		params.id = streamRestricted.id
		params.resourceClass = Stream
		params.resourceId = streamRestricted.id

		when:
		withFilters(action: "index") { controller.index() }
		then:
		thrown NotPermittedException
	}

	void "save grants Permissions"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/${canvasOwned.id}/permissions"
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id

		when:
		request.JSON = [user: other.username, operation: "read"] as JSON
		withFilters(action: "save") { controller.save() }
		then:
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.grant(me, _, _, Operation.READ) >> new Permission(user: other, operation: Operation.READ)
		response.status == 201
		response.json.user == other.username
		response.json.operation == "read"
	}

	void "delete revokes permissions"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/${canvasShared.id}/permissions/${canvasPermission.id}"
		params.id = canvasPermission.id
		params.resourceClass = Canvas
		params.resourceId = canvasShared.id

		when:
		withFilters(action: "delete") { controller.delete("${canvasPermission.id}") }
		then:
		response.status == 204
		1 * permissionService.canShare(me, _) >> true
		1 * permissionService.getPermissionsTo(_) >> [canvasPermission, *ownerPermissions]
		1 * permissionService._
	}

	void "can't give both 'user' and 'anonymous' arguments"() {
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/canvases/${canvasOwned.id}/permissions"
		params.id = canvasOwned.id
		params.resourceClass = Canvas
		params.resourceId = canvasOwned.id

		when:
		request.JSON = [anonymous: true, user: other.username, operation: "read"] as JSON
		withFilters(action: "save") { controller.save() }
		then:
		thrown InvalidArgumentsException
	}
}
