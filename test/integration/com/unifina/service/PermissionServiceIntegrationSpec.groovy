package com.unifina.service

import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.User
import grails.test.spock.IntegrationSpec
import grails.util.Holders

import java.security.AccessControlException

/*
	Ideally these tests would reside in {PermissionServiceSpec} as unit tests. However, due to spotty mocking of GORM,
	the behaviour of withCriteria differs in unit tests compared to a real database. Thus the tests were moved here so
	that they pass (as they should).
 */

class PermissionServiceIntegrationSpec extends IntegrationSpec {

	static transactional = false // Not ideal... but needed because of use of `withNewTransaction` in PermissionService

	PermissionService service

	User me, anotherUser, stranger, someone

	Product streamAllowed
	Product streamRestricted
	Product streamOwned
	Product streamPublic
	Product stream
	Permission dashReadPermission
	Permission dashAnonymousReadPermission

	Product anonymousStream

	void setup() {
		service = Holders.getApplicationContext().getBean(PermissionService)
		User.findByUsername("0x0000000000000000000000000000000000000011")?.delete(flush: true)
		User.findByUsername("0x0000000000000000000000000000000000000022")?.delete(flush: true)
		User.findByUsername("0x0000000000000000000000000000000000000033")?.delete(flush: true)
		User.findByUsername("0x0000000000000000000000000000000000000044")?.delete(flush: true)

		// Users
		me = new User(
			username: "0x0000000000000000000000000000000000000011",
			name: "me",
		).save(failOnError: true)

		anotherUser = new User(
			username: "0x0000000000000000000000000000000000000022",
			name: "him",
		).save(failOnError: true)

		stranger = new User(
			username: "0x0000000000000000000000000000000000000033",
			name: "stranger",
		).save(failOnError: true)

		someone = new User(
			username: "0x0000000000000000000000000000000000000044",
			name: "someone",
		).save(failOnError: true)

		// Products
		streamAllowed = new Product(name: "allowed")
		streamAllowed.id = "stream-allowed-id"
		streamAllowed.owner = anotherUser
		streamAllowed.save(failOnError: true)

		streamRestricted = new Product(name: "restricted")
		streamRestricted.id = "stream-restricted-id"
		streamRestricted.owner = anotherUser
		streamRestricted.save(failOnError: true)

		streamOwned = new Product(name: "owned")
		streamOwned.id = "stream-owned-id"
		streamOwned.owner = me
		streamOwned.save(failOnError: true)

		streamPublic = new Product(name: "public")
		streamPublic.id = "stream-public-id"
		streamPublic.owner = anotherUser
		streamPublic.save(failOnError: true)

		stream = new Product(name: "stream")
		stream.id = "stream-id"
		stream.owner = me
		stream.save(failOnError: true)

		service.systemGrantAll(anotherUser, streamAllowed)
		service.systemGrantAll(me, streamOwned)
		service.systemGrantAll(anotherUser, streamRestricted)
		service.systemGrantAll(anotherUser, streamPublic)

		// Set up the Permissions to the allowed resources
		dashReadPermission = service.systemGrant(me, streamAllowed, Permission.Operation.PRODUCT_GET)
		dashAnonymousReadPermission = service.systemGrantAnonymousAccess(streamPublic, Permission.Operation.PRODUCT_GET)

		anonymousStream = new Product(name: "anonymous stream")
		anonymousStream.id = "stream-id-3"
		anonymousStream.owner = anotherUser
		anonymousStream.save(validate: true, failOnError: true)
	}

	void cleanup() {
		Permission.findAllByProduct(streamAllowed)*.delete(flush: true)
		Permission.findAllByProduct(streamRestricted)*.delete(flush: true)
		Permission.findAllByProduct(streamOwned)*.delete(flush: true)
		Permission.findAllByProduct(streamPublic)*.delete(flush: true)
		Permission.findAllByProduct(anonymousStream)*.delete(flush: true)
		Permission.findAllByProduct(stream)*.delete(flush: true)

		streamAllowed?.delete(flush: true)
		streamRestricted?.delete(flush: true)
		streamOwned?.delete(flush: true)
		streamPublic?.delete(flush: true)
		stream?.delete(flush: true)
		anonymousStream?.delete(flush: true)

		me?.delete(flush: true)
		anotherUser?.delete(flush: true)
		stranger?.delete(flush: true)
		someone?.delete(flush: true)
	}

	void "get closure filtering works as expected"() {
		expect:
		service.get(me, Permission.Operation.PRODUCT_GET, { like("name", "%ll%") }) == [streamAllowed]
		service.get(me, Permission.Operation.PRODUCT_SHARE) == [streamOwned]
		service.get(me, Permission.Operation.PRODUCT_SHARE, { like("name", "%ll%") }) == []
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_SHARE)
		then:
		service.get(stranger, Permission.Operation.PRODUCT_GET) == [streamOwned]
		service.get(stranger, Permission.Operation.PRODUCT_SHARE) == [streamOwned]

		expect:
		!(streamOwned in service.get(anotherUser, Permission.Operation.PRODUCT_GET))

		when: "stranger shares read access"
		service.grant(stranger, streamOwned, anotherUser, Permission.Operation.PRODUCT_GET)
		then:
		streamOwned in service.get(anotherUser, Permission.Operation.PRODUCT_GET)
		!(streamOwned in service.get(anotherUser, Permission.Operation.PRODUCT_SHARE))

		when:
		service.revoke(stranger, streamOwned, anotherUser, Permission.Operation.PRODUCT_GET)
		then:
		!(streamOwned in service.get(anotherUser, Permission.Operation.PRODUCT_GET))

		when: "of course, it's silly to revoke 'stream_share' access since it might already been re-shared..."
		service.revoke(me, streamOwned, stranger, Permission.Operation.PRODUCT_SHARE)
		service.grant(stranger, streamOwned, anotherUser, Permission.Operation.PRODUCT_SHARE)
		then:
		thrown AccessControlException
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_SHARE)
		when:
		service.revoke(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		then: "by default, revoke all access"
		service.get(stranger, Permission.Operation.PRODUCT_GET) == []
		service.get(stranger, Permission.Operation.PRODUCT_SHARE) == [streamOwned]
	}

	void "revocation is granular"() {
		setup:
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_SHARE)
		when:
		service.revoke(me, streamOwned, stranger, Permission.Operation.PRODUCT_SHARE)
		then: "only 'share' access is revoked"
		service.get(stranger, Permission.Operation.PRODUCT_GET) == [streamOwned]
		service.get(stranger, Permission.Operation.PRODUCT_SHARE) == []
	}

	void "get does not return expired permissions"() {
		def p1 = service.systemGrant(someone, streamRestricted, Permission.Operation.PRODUCT_GET)
		def p2 = service.systemGrant(someone, streamOwned, Permission.Operation.PRODUCT_GET)
		p1.endsAt = new Date(System.currentTimeMillis() - 1000 * 60000)
		p2.endsAt = new Date(0)
		p1.save(failOnError: true)
		p2.save(failOnError: true, flush: true)

		expect:
		service.get(someone, Permission.Operation.PRODUCT_GET) == []
	}

	void "get returns non-expired permissions"() {
		def p1 = service.systemGrant(someone, streamRestricted, Permission.Operation.PRODUCT_GET)
		def p2 = service.systemGrant(someone, streamOwned, Permission.Operation.PRODUCT_GET)
		p1.endsAt = new Date(System.currentTimeMillis() + 10000)
		p2.endsAt = new Date(System.currentTimeMillis() + 1000000)
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		expect:
		service.get(someone, Permission.Operation.PRODUCT_GET) as Set == [streamOwned, streamRestricted] as Set
	}

	void "getAll lists public resources"() {
		expect:
		(service.getAll(me, Permission.Operation.PRODUCT_GET) as Set).containsAll([streamOwned, streamPublic, streamAllowed] as Set)
		(service.getAll(anotherUser, Permission.Operation.PRODUCT_GET) as Set).containsAll([streamAllowed, streamRestricted, streamPublic] as Set)
		service.getAll(stranger, Permission.Operation.PRODUCT_GET).contains(streamPublic)
	}

	void "getAll returns public resources on bad/null user"() {
		expect:
		service.get(new User(), Permission.Operation.PRODUCT_GET) == []
		service.get(null, Permission.Operation.PRODUCT_GET) == []
		service.getAll(new User(), Permission.Operation.PRODUCT_GET).contains(streamPublic)
		service.getAll(null, Permission.Operation.PRODUCT_GET).contains(streamPublic)
	}

	void "granting and revoking read rights"() {
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		then:
		service.get(stranger, Permission.Operation.PRODUCT_GET) == [streamOwned]

		when:
		service.revoke(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		then:
		service.get(stranger, Permission.Operation.PRODUCT_GET) == []
	}

	void "granting and revoking share rights"() {
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_SHARE)
		then:
		service.get(stranger, Permission.Operation.PRODUCT_SHARE) == [streamOwned]
	}

	void "granting works (roughly) idempotently"() {
		expect:
		service.get(stranger, Permission.Operation.PRODUCT_GET) == []
		when: "double-granting still has the same effect: there exists a permission for user to resource"
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		then: "now you see it..."
		service.get(stranger, Permission.Operation.PRODUCT_GET) == [streamOwned]
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		service.revoke(me, streamOwned, stranger, Permission.Operation.PRODUCT_GET)
		then: "now you don't."
		service.get(stranger, Permission.Operation.PRODUCT_GET) == []
	}

	void "retrieve all readable Streams correctly"() {
		expect:
		service.get(me, Permission.Operation.PRODUCT_GET) as Set == [streamOwned, streamAllowed] as Set
		service.get(anotherUser, Permission.Operation.PRODUCT_GET) as Set == [streamAllowed, streamRestricted, streamPublic] as Set
		service.get(stranger, Permission.Operation.PRODUCT_GET) == []
	}

	void "getPermissionsTo with Operation returns all permissions for the given resource"() {
		setup:
		List<Permission> beforeRead = service.getPermissionsTo(streamOwned, Permission.Operation.PRODUCT_GET)
		List<Permission> beforeWrite = service.getPermissionsTo(streamOwned, Permission.Operation.PRODUCT_EDIT)
		Permission perm = service.systemGrant(stranger, streamOwned, Permission.Operation.PRODUCT_GET)
		List<Permission> afterRead = service.getPermissionsTo(streamOwned, Permission.Operation.PRODUCT_GET)
		List<Permission> afterWrite = service.getPermissionsTo(streamOwned, Permission.Operation.PRODUCT_EDIT)
		List<Permission> all = service.getPermissionsTo(streamOwned)
		List<Permission> allOperations = new ArrayList<Permission>()
		Permission.Operation.productOperations().collect { Permission.Operation op ->
			allOperations.addAll(service.getPermissionsTo(streamOwned, op))
		}
		expect:
		!beforeRead.contains(perm)
		afterRead.contains(perm)
		beforeRead.size() + 1 == afterRead.size()
		beforeWrite.size() == afterWrite.size()
		all.size() == allOperations.size()
	}

	void "getPermissionsTo returns all permissions for the given resource"() {
		setup:
		def perm = service.systemGrant(stranger, streamOwned, Permission.Operation.PRODUCT_GET)
		expect:
		service.getPermissionsTo(streamOwned).size() == Permission.Operation.productOperations().size() + 1
		service.getPermissionsTo(streamOwned).contains(perm)
		service.getPermissionsTo(streamAllowed).size() == Permission.Operation.productOperations().size() + 1
		service.getPermissionsTo(streamAllowed).contains(dashReadPermission)
		service.getPermissionsTo(streamRestricted).size() == Permission.Operation.productOperations().size()
		service.getPermissionsTo(streamRestricted)[0].user == anotherUser
	}

	void "cannot revoke only share permission"() {
		setup:
		service.systemGrant(me, stream, Permission.Operation.PRODUCT_EDIT)
		service.systemGrant(me, stream, Permission.Operation.PRODUCT_GET)
		service.systemGrant(me, stream, Permission.Operation.PRODUCT_SHARE)

		when:
		service.systemRevoke(me, stream, Permission.Operation.PRODUCT_SHARE)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${stream}"
	}

	void "check anonymous permission"() {
		when:
		service.systemGrantAnonymousAccess(anonymousStream, Permission.Operation.PRODUCT_GET)
		then:
		service.checkAnonymousAccess(anonymousStream, Permission.Operation.PRODUCT_GET) == true

		when:
		service.systemRevokeAnonymousAccess(anonymousStream, Permission.Operation.PRODUCT_GET)
		then:
		service.checkAnonymousAccess(anonymousStream, Permission.Operation.PRODUCT_GET) == false
	}
}
