package com.unifina.service

import com.unifina.domain.*
import grails.test.spock.IntegrationSpec
import grails.util.Holders

class PermissionServiceDeleteIntegrationSpec extends IntegrationSpec {
	static transactional = false
	PermissionService service

	User user
	Canvas resource
	User another
	Canvas anotherResource

	void setup() {
		service = Holders.getApplicationContext().getBean(PermissionService)
		user = new User(name: "name", username: "me@me.com", password: "x")
		user.save(validate: true, failOnError: true)

		resource = new Canvas()
		resource.id = "canvas-id-1"
		resource.save(validate: true, failOnError: true)
		another = new User(name: "another", username: "another@example.com", password: "x")
		another.save(validate: true, failOnError: true)

		anotherResource = new Canvas()
		anotherResource.id = "another-canvas-id-2"
		anotherResource.save(validate: true, failOnError: true)
	}

	void cleanup() {
		Permission.findAllByCanvas(resource)*.delete(flush: true)
		Permission.findAllByCanvas(anotherResource)*.delete(flush: true)
		resource.delete(flush: true)
		user.delete(flush: true)
		another.delete(flush: true)
		anotherResource.delete(flush: true)
	}

	void "user with share permission to resource can delete another user's permission to same resource"() {
		setup:
		Permission sharePermission = service.systemGrant(user, resource, Permission.Operation.CANVAS_SHARE)
		Permission permission = service.systemGrant(another, resource, Permission.Operation.CANVAS_GET)

		Resource res = new Resource(Canvas, resource.id)

		when:
		service.deletePermission(permission.id, res, user)

		then:
		Permission.findById(permission.id) == null
	}

	void "user without share permission to resource can't delete another user's permission to same resource"() {
		setup:
		Permission permission = service.systemGrant(another, resource, Permission.Operation.CANVAS_GET)
		Resource res = new Resource(Canvas, resource.id)

		when:
		service.deletePermission(permission.id, res, user)

		then:
		def e = thrown(NotPermittedException)
		e.statusCode == 403
	}

	void "user without share permission to resource can delete their own permission to resource"() {
		setup:
		Permission permission = service.systemGrant(user, resource, Permission.Operation.CANVAS_GET)
		Resource res = new Resource(Canvas, resource.id)

		when:
		service.deletePermission(permission.id, res, user)

		then:
		Permission.findById(permission.id) == null
	}

	void "user with share permission to resource can't delete another user's permission to another resource"() {
		setup:
		Permission permission = service.systemGrant(user, resource, Permission.Operation.CANVAS_SHARE)
		Permission anotherPermission = service.systemGrant(another, anotherResource, Permission.Operation.CANVAS_SHARE)
		Resource res = new Resource(Canvas, anotherResource.id)

		when:
		service.deletePermission(anotherPermission.id, res, user)

		then:
		def e = thrown(NotPermittedException)
		e.statusCode == 403
	}

	void "deletePermission() deletes permission"() {
		setup:
		Resource res = new Resource(Canvas, resource.id)
		User apiUser = user
		Permission share = service.systemGrant(apiUser, resource, Permission.Operation.CANVAS_SHARE)
		Permission share2 = service.systemGrant(apiUser, resource, Permission.Operation.CANVAS_SHARE)
		Permission p = service.systemGrant(apiUser, resource, Permission.Operation.CANVAS_INTERACT)
		when:
		service.deletePermission(p.id, res, apiUser)
		then:
		Permission.findById(p.id) == null
	}

	void "deletePermission() throws NotFoundException when permission id not found"() {
		setup:
		Resource res = new Resource(Canvas, resource.id)
		User apiUser = user
		Permission share = service.systemGrant(apiUser, resource, Permission.Operation.CANVAS_SHARE)
		Permission p = service.systemGrant(apiUser, resource, Permission.Operation.CANVAS_INTERACT)
		when:
		service.deletePermission(null, res, apiUser)
		then:
		def e = thrown(NotFoundException)
		e.type == "Canvas"
		e.id == null
	}
}
