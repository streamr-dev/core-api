package com.streamr.core.service

import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.Resource
import com.streamr.core.domain.User
import grails.test.spock.IntegrationSpec
import grails.util.Holders

class PermissionServiceDeleteIntegrationSpec extends IntegrationSpec {
	static transactional = false
	PermissionService service

	User user
	Product resource
	User another
	Product anotherResource

	void setup() {
		service = Holders.getApplicationContext().getBean(PermissionService)
		user = new User(name: "name", username: "0x0000000000000000000000000000000000000001")
		user.save(validate: true, failOnError: true)

		resource = new Product(name: "stream 1")
		resource.id = "stream-id-1"
		resource.owner = user
		resource.save(validate: true, failOnError: true)

		another = new User(name: "another", username: "0x0000000000000000000000000000000000000002")
		another.save(validate: true, failOnError: true)

		anotherResource = new Product(name: "stream 2")
		anotherResource.id = "another-stream-id-2"
		anotherResource.owner = another
		anotherResource.save(validate: true, failOnError: true)
	}

	void cleanup() {
		Permission.findAllByProduct(resource)*.delete(flush: true)
		Permission.findAllByProduct(anotherResource)*.delete(flush: true)
		resource.delete(flush: true)
		anotherResource.delete(flush: true)
		user.delete(flush: true)
		another.delete(flush: true)
	}

	void "user with share permission to resource can delete another user's permission to same resource"() {
		setup:
		Permission sharePermission = service.systemGrant(user, resource, Permission.Operation.PRODUCT_SHARE)
		Permission permission = service.systemGrant(another, resource, Permission.Operation.PRODUCT_GET)

		Resource res = new Resource(resource.id)

		when:
		service.deletePermission(permission.id, res, user)

		then:
		Permission.get(permission.id) == null
	}

	void "user without share permission to resource can't delete another user's permission to same resource"() {
		setup:
		Permission permission = service.systemGrant(another, resource, Permission.Operation.PRODUCT_GET)
		Resource res = new Resource(resource.id)

		when:
		service.deletePermission(permission.id, res, user)

		then:
		def e = thrown(NotPermittedException)
		e.statusCode == 403
	}

	void "user without share permission to resource can delete their own permission to resource"() {
		setup:
		Permission permission = service.systemGrant(user, resource, Permission.Operation.PRODUCT_GET)
		Resource res = new Resource(resource.id)

		when:
		service.deletePermission(permission.id, res, user)

		then:
		Permission.get(permission.id) == null
	}

	void "user with share permission to resource can't delete another user's permission to another resource"() {
		setup:
		Permission permission = service.systemGrant(user, resource, Permission.Operation.PRODUCT_SHARE)
		Permission anotherPermission = service.systemGrant(another, anotherResource, Permission.Operation.PRODUCT_SHARE)
		Resource res = new Resource(anotherResource.id)

		when:
		service.deletePermission(anotherPermission.id, res, user)

		then:
		def e = thrown(NotPermittedException)
		e.statusCode == 403
	}

	void "deletePermission() deletes permission"() {
		setup:
		Resource res = new Resource(resource.id)
		User apiUser = user
		Permission share = service.systemGrant(apiUser, resource, Permission.Operation.PRODUCT_SHARE)
		Permission share2 = service.systemGrant(apiUser, resource, Permission.Operation.PRODUCT_SHARE)
		Permission p = service.systemGrant(apiUser, resource, Permission.Operation.PRODUCT_DELETE)
		when:
		service.deletePermission(p.id, res, apiUser)
		then:
		Permission.get(p.id) == null
	}

	void "deletePermission() throws NotFoundException when permission id not found"() {
		setup:
		Resource res = new Resource(resource.id)
		User apiUser = user
		Permission share = service.systemGrant(apiUser, resource, Permission.Operation.PRODUCT_SHARE)
		Permission p = service.systemGrant(apiUser, resource, Permission.Operation.PRODUCT_DELETE)
		when:
		service.deletePermission(null, res, apiUser)
		then:
		def e = thrown(NotFoundException)
		e.type == "Product"
		e.id == null
	}
}
