package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.domain.*
import com.unifina.domain.Permission.Operation
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.security.AccessControlException

/*
	If you get weird test failures, it may be due to spotty GORM and mocked criteria queries.
	You might want to try PermissionServiceIntegrationSpec instead.
 */

@TestMixin(GrailsUnitTestMixin)
@TestFor(PermissionService)
@Mock([User, Permission, Product])
class PermissionServiceSpec extends BeanMockingSpecification {
	User me
	User anotherUser
	User stranger
	Product streamAllowed
	Product streamRestricted
	Product streamOwned
	Product streamPublic
	Permission streamAnonymousReadPermission

	ProductService streamService

	def setup() {
		// Users
		me = new User(username: "me").save(validate: false)
		anotherUser = new User(username: "him").save(validate: false)
		stranger = new User(username: "stranger").save(validate: false)

		// Products
		streamAllowed = new Product(name: "allowed")
		streamAllowed.id = "allowed"
		streamAllowed.save(validate: false)
		streamRestricted = new Product(name: "restricted")
		streamRestricted.id = "restricted"
		streamRestricted.save(validate: false)
		streamOwned = new Product(name: "owned")
		streamOwned.id = "owned"
		streamOwned.save(validate: false)
		streamPublic = new Product(name: "public")
		streamPublic.id = "public"
		streamPublic.save(validate: false)

		service.systemGrantAll(anotherUser, streamAllowed)
		service.systemGrantAll(me, streamOwned)
		service.systemGrantAll(anotherUser, streamRestricted)
		service.systemGrantAll(anotherUser, streamPublic)

		// Set up the Permissions to the allowed resources
		streamAnonymousReadPermission = service.systemGrantAnonymousAccess(streamPublic, Operation.PRODUCT_GET)

		streamService = mockBean(ProductService, Mock(ProductService))
	}

	void "access denied to non-permitted Product"() {
		expect:
		!service.check(me, streamRestricted, Permission.Operation.PRODUCT_GET)
	}

	void "non-permitted third-parties have no access to resources"() {
		expect:
		!service.check(stranger, streamAllowed, Permission.Operation.PRODUCT_GET)
		!service.check(stranger, streamRestricted, Permission.Operation.PRODUCT_GET)
		!service.check(stranger, streamOwned, Permission.Operation.PRODUCT_GET)
	}

	void "canRead returns false on bad inputs"() {
		expect:
		!service.check(null, streamAllowed, Permission.Operation.PRODUCT_GET)
		!service.check(me, new Product(), Permission.Operation.PRODUCT_GET)
		!service.check(me, null, Permission.Operation.PRODUCT_GET)
	}

	void "getPermissionsTo(resource, userish) returns permissions for single user"() {
		expect:
		service.getPermissionsTo(streamOwned, anotherUser) == []
		service.getPermissionsTo(streamOwned, stranger) == []
		service.getPermissionsTo(streamOwned, null) == []
		service.getPermissionsTo(streamAllowed, anotherUser).size() == Operation.productOperations().size()
		service.getPermissionsTo(streamAllowed, stranger) == []
		service.getPermissionsTo(streamAllowed, null) == []
		service.getPermissionsTo(streamRestricted, me) == []
		service.getPermissionsTo(streamRestricted, anotherUser).size() == Operation.productOperations().size()
		service.getPermissionsTo(streamRestricted, stranger) == []
		service.getPermissionsTo(streamRestricted, null) == []
		service.getPermissionsTo(streamPublic, me)[0].operation == Operation.PRODUCT_GET
		service.getPermissionsTo(streamPublic, anotherUser).size() == Operation.productOperations().size()
		service.getPermissionsTo(streamPublic, stranger)[0].operation == Operation.PRODUCT_GET
		service.getPermissionsTo(streamPublic, null)[0].operation == Operation.PRODUCT_GET
	}

	void "grant and revoke throw for non-'share'-access users"() {
		when:
		service.grant(me, streamAllowed, stranger, Permission.Operation.PRODUCT_GET)
		then:
		thrown AccessControlException

		when:
		service.revoke(stranger, streamRestricted, me, Permission.Operation.PRODUCT_GET)
		then:
		thrown AccessControlException
	}

	void "stranger can read public resources with anonymous read access"() {
		expect: "... but not more than read"
		service.check(stranger, streamPublic, Permission.Operation.PRODUCT_GET)
		!service.check(stranger, streamPublic, Permission.Operation.PRODUCT_EDIT)
		!service.check(stranger, streamPublic, Permission.Operation.PRODUCT_SHARE)
	}

	void "verify does not throw if permission exists"() {
		when:
		service.verify(stranger, streamPublic, Operation.PRODUCT_GET)
		then:
		notThrown(NotPermittedException)
	}

	void "verify throws if permission does not exist"() {
		when:
		service.verify(stranger, streamPublic, Operation.PRODUCT_EDIT)
		then:
		def e = thrown(NotPermittedException)
		e.message == "stranger does not have permission to product_edit Product (id public)"
	}

	void "systemRevokeAnonymousAccess() revokes anonymous access on a resource"() {
		assert Permission.exists(streamAnonymousReadPermission.id)
		assert service.check(null, streamPublic, Permission.Operation.PRODUCT_GET)

		when:
		service.systemRevokeAnonymousAccess(streamPublic, Operation.PRODUCT_GET)

		then:
		!Permission.exists(streamAnonymousReadPermission.id)
		!service.check(null, streamPublic, Permission.Operation.PRODUCT_GET)
	}

	void "check() returns false if permission with endsAt set in past"() {
		def p = service.systemGrant(stranger, streamOwned, Operation.PRODUCT_GET)
		p.endsAt = new Date(0)
		p.save(failOnError: true)

		expect:
		!service.check(stranger, streamOwned, Operation.PRODUCT_GET)
	}

	void "check() returns true if permission with endsAt set in future"() {
		def p = service.systemGrant(stranger, streamOwned, Operation.PRODUCT_GET)
		p.endsAt = new Date(System.currentTimeMillis() + 60000)
		p.save(failOnError: true)

		expect:
		service.check(stranger, streamOwned, Operation.PRODUCT_GET)
	}

	void "cleanUpExpiredPermissions() deletes permissions that already ended"() {
		User testUser = new User(username: "0x0000000000000000000000000000000000000001").save(validate: false)
		Product p = new Product(name: "test product")
		p.id = "testProductId"
		p.save(validate: false)

		assert Permission.findAllByProduct(p).size() == 0

		when:
		Permission p1 = service.systemGrant(testUser, p, Operation.PRODUCT_GET)
		p1.endsAt = new Date(0)
		p1.save(failOnError: true)
		Permission p2 = service.systemGrant(testUser, p, Operation.PRODUCT_EDIT)
		p2.endsAt = new Date(System.currentTimeMillis() + 60000)
		p2.save(failOnError: true)

		then:
		Permission.findAllByProduct(p).size() == 2

		when:
		service.cleanUpExpiredPermissions()

		then:
		Permission.findAllByProduct(p).size() == 1
		!service.check(testUser, p, Operation.PRODUCT_GET)
		service.check(testUser, p, Permission.Operation.PRODUCT_EDIT)
	}

	Product newStream(String id, User owner) {
		Product s = new Product(name: "Product " + id)
		s.id = id
		s.owner = owner
		return s.save(validate: true, failOnError: true, flush: true)
	}

	void "save() creates a new user with permission if unknown ethereum address provided"() {
		setup:
		EthereumUserService ethereumUserService = mockBean(EthereumUserService, Mock(EthereumUserService))
		String ethUserUsername = "0xa50E97f6a98dD992D9eCb8207c2Aa58F54970729"
		User createdEthUser = new User(username: ethUserUsername, name: "Ethereum User")
		createdEthUser.save(validate: true, failOnError: true)
		Product stream = newStream("own-id", me)
		Resource res = new Resource(stream.id)
		User apiUser = me
		Operation op = Operation.PRODUCT_GET
		service.systemGrant(me, stream, Operation.PRODUCT_SHARE)
		when:
		service.savePermissionForEthereumAccount(ethUserUsername, apiUser, op, res, SignupMethod.UNKNOWN)
		then:
		//1 * streamService.getStream(stream.id) >> stream
		1 * ethereumUserService.getOrCreateFromEthereumAddress(ethUserUsername, SignupMethod.UNKNOWN) >> createdEthUser
		service.check(createdEthUser, stream, op)
	}

	void "save anonymous permission"() {
		setup:
		User me = new User(id: 1, username: "me@me.net").save(validate: false)
		Product stream = newStream("own-id", me)
		Resource res = new Resource(stream.id)
		User apiUser = me
		Operation op = Operation.PRODUCT_GET
		service.systemGrant(apiUser, stream, Operation.PRODUCT_SHARE)
		when:
		service.saveAnonymousPermission(apiUser, op, res)
		then:
		//1 * streamService.getStream(stream.id) >> stream
		service.check(apiUser, stream, op)
	}

	void "findAllPermissions() won't show list of permissions without 'share' permission (string id)"() {
		setup:
		Product stream = newStream("stream-id", me)
		Resource resource = new Resource(stream.id)
		User apiUser = me
		boolean subscriptions = false
		when:
		service.findAllPermissions(resource, apiUser, subscriptions)
		then:
		//1 * streamService.getStream(stream.id) >> stream
		thrown NotPermittedException
	}

	void "findPermission finds permission"() {
		setup:
		Product stream = newStream("stream-id", me)
		Resource resource = new Resource(stream.id)
		User apiUser = me
		service.systemGrant(apiUser, stream, Operation.PRODUCT_SHARE)
		Permission p = service.systemGrant(apiUser, stream, Operation.PRODUCT_EDIT)
		p.save(flush: true)
		when:
		//1 * streamService.getStream(stream.id) >> stream
		Permission permission = service.findPermission(p.id, resource, apiUser)
		then:
		p == permission
	}

	void "findPermission throws NotFoundException when permission is not found "() {
		setup:
		Product productOwned = newStream("product-id", me)
		Resource resource = new Resource(productOwned.id)
		User apiUser = me
		service.systemGrant(apiUser, productOwned, Operation.PRODUCT_SHARE)
		Permission p = service.systemGrant(apiUser, productOwned, Operation.PRODUCT_DELETE)
		p.save(flush: true)
		when:
		Permission permission = service.findPermission(null, resource, apiUser)
		then:
		def e = thrown(NotFoundException)
		e.type == "Product"
		e.id == null
	}

	void "index won't show list of permissions without 'share' permission (Product using id)"() {
		setup:
		Product stream = newStream("stream-id", me)
		Resource resource = new Resource(stream.id)
		User apiUser = me
		boolean subscriptions = false
		when:
		service.findAllPermissions(resource, apiUser, subscriptions)
		then:
		//1 * streamService.getStream(stream.id) >> stream
		thrown NotPermittedException
	}
}
