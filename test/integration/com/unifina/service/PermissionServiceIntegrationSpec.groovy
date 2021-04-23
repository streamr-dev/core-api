package com.unifina.service

import com.unifina.domain.Permission
import com.unifina.domain.SignupInvite
import com.unifina.domain.Stream
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

	Stream streamAllowed
	Stream streamRestricted
	Stream streamOwned
	Stream streamPublic
	Stream stream
	Permission dashReadPermission
	Permission dashAnonymousReadPermission

	SignupInvite invite

	Stream anonymousStream

	void setup() {
		service = Holders.getApplicationContext().getBean(PermissionService)
		User.findByUsername("me-permission-service-integration-spec@streamr.network")?.delete(flush: true)
		User.findByUsername("him-permission-service-integration-spec@streamr.network")?.delete(flush: true)
		User.findByUsername("stranger-permission-service-integration-spec@streamr.network")?.delete(flush: true)
		User.findByUsername("someone-service-integration-spec@streamr.network")?.delete(flush: true)

		// Users
		me = new User(
			username: "me-permission-service-integration-spec@streamr.network",
			name: "me",
		).save(failOnError: true)

		anotherUser = new User(
			username: "him-permission-service-integration-spec@streamr.network",
			name: "him",
		).save(failOnError: true)

		stranger = new User(
			username: "stranger-permission-service-integration-spec@streamr.network",
			name: "stranger",
		).save(failOnError: true)

		someone = new User(
			username: "someone-service-integration-spec@streamr.network",
			name: "someone",
		).save(failOnError: true)

		// Streams
		streamAllowed = new Stream(name: "allowed")
		streamAllowed.id = "stream-allowed-id"
		streamAllowed.save(failOnError: true)
		streamRestricted = new Stream(name: "restricted")
		streamRestricted.id = "stream-restricted-id"
		streamRestricted.save(failOnError: true)
		streamOwned = new Stream(name: "owned")
		streamOwned.id = "stream-owned-id"
		streamOwned.save(failOnError: true)
		streamPublic = new Stream(name: "public")
		streamPublic.id = "stream-public-id"
		streamPublic.save(failOnError: true)

		stream = new Stream(name: "stream")
		stream.id = "stream-id"
		stream.save(failOnError: true)

		service.systemGrantAll(anotherUser, streamAllowed)
		service.systemGrantAll(me, streamOwned)
		service.systemGrantAll(anotherUser, streamRestricted)
		service.systemGrantAll(anotherUser, streamPublic)

		// Set up the Permissions to the allowed resources
		dashReadPermission = service.systemGrant(me, streamAllowed, Permission.Operation.STREAM_GET)
		dashAnonymousReadPermission = service.systemGrantAnonymousAccess(streamPublic, Permission.Operation.STREAM_GET)

		// Sign-up invitations can also receive Permissions; they will later be converted to User permissions
		invite = new SignupInvite(email: "him-permission-service-integration-spec@streamr.network", code: "sikritCode", sent: true, used: false).save(validate: false, flush: true)

		anonymousStream = new Stream(name: "anonymous stream")
		anonymousStream.id = "stream-id-3"
		anonymousStream.save(validate: true, failOnError: true)
	}

	void cleanup() {
		Permission.findAllByStream(streamAllowed)*.delete(flush: true)
		Permission.findAllByStream(streamRestricted)*.delete(flush: true)
		Permission.findAllByStream(streamOwned)*.delete(flush: true)
		Permission.findAllByStream(streamPublic)*.delete(flush: true)
		Permission.findAllByStream(anonymousStream)*.delete(flush: true)
		Permission.findAllByStream(stream)*.delete(flush: true)

		streamAllowed?.delete(flush: true)
		streamRestricted?.delete(flush: true)
		streamOwned?.delete(flush: true)
		streamPublic?.delete(flush: true)
		stream?.delete(flush: true)

		me?.delete(flush: true)
		anotherUser?.delete(flush: true)
		stranger?.delete(flush: true)
		someone?.delete(flush: true)

		invite?.delete(flush: true)

		anonymousStream?.delete(flush: true)
	}

	void "get closure filtering works as expected"() {
		expect:
		service.get(Stream, me, Permission.Operation.STREAM_GET) { like("name", "%ll%") } == [streamAllowed]
		service.get(Stream, me, Permission.Operation.STREAM_SHARE) == [streamOwned]
		service.get(Stream, me, Permission.Operation.STREAM_SHARE) { like("name", "%ll%") } == []
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_SHARE)
		then:
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == [streamOwned]
		service.get(Stream, stranger, Permission.Operation.STREAM_SHARE) == [streamOwned]

		expect:
		!(streamOwned in service.get(Stream, anotherUser, Permission.Operation.STREAM_GET))

		when: "stranger shares read access"
		service.grant(stranger, streamOwned, anotherUser, Permission.Operation.STREAM_GET)
		then:
		streamOwned in service.get(Stream, anotherUser, Permission.Operation.STREAM_GET)
		!(streamOwned in service.get(Stream, anotherUser, Permission.Operation.STREAM_SHARE))

		when:
		service.revoke(stranger, streamOwned, anotherUser, Permission.Operation.STREAM_GET)
		then:
		!(streamOwned in service.get(Stream, anotherUser, Permission.Operation.STREAM_GET))

		when: "of course, it's silly to revoke 'stream_share' access since it might already been re-shared..."
		service.revoke(me, streamOwned, stranger, Permission.Operation.STREAM_SHARE)
		service.grant(stranger, streamOwned, anotherUser, Permission.Operation.STREAM_SHARE)
		then:
		thrown AccessControlException
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_SHARE)
		when:
		service.revoke(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		then: "by default, revoke all access"
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == []
		service.get(Stream, stranger, Permission.Operation.STREAM_SHARE) == [streamOwned]
	}

	void "revocation is granular"() {
		setup:
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_SHARE)
		when:
		service.revoke(me, streamOwned, stranger, Permission.Operation.STREAM_SHARE)
		then: "only 'share' access is revoked"
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == [streamOwned]
		service.get(Stream, stranger, Permission.Operation.STREAM_SHARE) == []
	}

	void "get does not return expired permissions"() {
		def p1 = service.systemGrant(someone, streamRestricted, Permission.Operation.STREAM_GET)
		def p2 = service.systemGrant(someone, streamOwned, Permission.Operation.STREAM_GET)
		p1.endsAt = new Date(System.currentTimeMillis() - 1000 * 60000)
		p2.endsAt = new Date(0)
		p1.save(failOnError: true)
		p2.save(failOnError: true, flush: true)

		expect:
		service.get(Stream, someone, Permission.Operation.STREAM_GET) == []
	}

	void "get returns non-expired permissions"() {
		def p1 = service.systemGrant(someone, streamRestricted, Permission.Operation.STREAM_GET)
		def p2 = service.systemGrant(someone, streamOwned, Permission.Operation.STREAM_GET)
		p1.endsAt = new Date(System.currentTimeMillis() + 10000)
		p2.endsAt = new Date(System.currentTimeMillis() + 1000000)
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		expect:
		service.get(Stream, someone, Permission.Operation.STREAM_GET) as Set == [streamOwned, streamRestricted] as Set
	}

	void "getAll lists public resources"() {
		expect:
		(service.getAll(Stream, me, Permission.Operation.STREAM_GET) as Set).containsAll([streamOwned, streamPublic, streamAllowed] as Set)
		(service.getAll(Stream, anotherUser, Permission.Operation.STREAM_GET) as Set).containsAll([streamAllowed, streamRestricted, streamPublic] as Set)
		service.getAll(Stream, stranger, Permission.Operation.STREAM_GET).contains(streamPublic)
	}

	void "getAll returns public resources on bad/null user"() {
		expect:
		service.get(Stream, new User(), Permission.Operation.STREAM_GET) == []
		service.get(Stream, null, Permission.Operation.STREAM_GET) == []
		service.getAll(Stream, new User(), Permission.Operation.STREAM_GET).contains(streamPublic)
		service.getAll(Stream, null, Permission.Operation.STREAM_GET).contains(streamPublic)
	}

	void "granting and revoking read rights"() {
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		then:
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == [streamOwned]

		when:
		service.revoke(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		then:
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == []
	}

	void "granting and revoking share rights"() {
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_SHARE)
		then:
		service.get(Stream, stranger, Permission.Operation.STREAM_SHARE) == [streamOwned]
	}

	void "granting works (roughly) idempotently"() {
		expect:
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == []
		when: "double-granting still has the same effect: there exists a permission for user to resource"
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		then: "now you see it..."
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == [streamOwned]
		when:
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.grant(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		service.revoke(me, streamOwned, stranger, Permission.Operation.STREAM_GET)
		then: "now you don't."
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == []
	}

	void "retrieve all readable Streams correctly"() {
		expect:
		service.get(Stream, me, Permission.Operation.STREAM_GET) as Set == [streamOwned, streamAllowed] as Set
		service.get(Stream, anotherUser, Permission.Operation.STREAM_GET) as Set == [streamAllowed, streamRestricted, streamPublic] as Set
		service.get(Stream, stranger, Permission.Operation.STREAM_GET) == []
	}

	void "getPermissionsTo with Operation returns all permissions for the given resource"() {
		setup:
		List<Permission> beforeRead = service.getPermissionsTo(streamOwned, Permission.Operation.STREAM_GET)
		List<Permission> beforeWrite = service.getPermissionsTo(streamOwned, Permission.Operation.STREAM_EDIT)
		Permission perm = service.systemGrant(stranger, streamOwned, Permission.Operation.STREAM_GET)
		List<Permission> afterRead = service.getPermissionsTo(streamOwned, Permission.Operation.STREAM_GET)
		List<Permission> afterWrite = service.getPermissionsTo(streamOwned, Permission.Operation.STREAM_EDIT)
		List<Permission> all = service.getPermissionsTo(streamOwned)
		List<Permission> allOperations = new ArrayList<Permission>()
		Permission.Operation.streamOperations().collect { Permission.Operation op ->
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
		def perm = service.systemGrant(stranger, streamOwned, Permission.Operation.STREAM_GET)
		expect:
		service.getPermissionsTo(streamOwned).size() == 7
		service.getPermissionsTo(streamOwned).contains(perm)
		service.getPermissionsTo(streamAllowed).size() == 7
		service.getPermissionsTo(streamAllowed).contains(dashReadPermission)
		service.getPermissionsTo(streamRestricted).size() == 6
		service.getPermissionsTo(streamRestricted)[0].user == anotherUser
	}

	void "getNonExpiredPermissionsTo with Operation returns all non-expired permissions for the given resource"() {
		// craft an expired permission
		service.systemGrant(me, stream, Permission.Operation.STREAM_EDIT, null, new Date(0))
		setup:
		List<Permission> beforeRead = service.getNonExpiredPermissionsTo(streamOwned, Permission.Operation.STREAM_GET)
		List<Permission> beforeWrite = service.getNonExpiredPermissionsTo(streamOwned, Permission.Operation.STREAM_EDIT)
		Permission perm = service.systemGrant(stranger, streamOwned, Permission.Operation.STREAM_GET)
		List<Permission> afterRead = service.getNonExpiredPermissionsTo(streamOwned, Permission.Operation.STREAM_GET)
		List<Permission> afterWrite = service.getNonExpiredPermissionsTo(streamOwned, Permission.Operation.STREAM_EDIT)
		List<Permission> testDashPerms = service.getNonExpiredPermissionsTo(stream, Permission.Operation.STREAM_EDIT)
		expect:
		!beforeRead.contains(perm)
		afterRead.contains(perm)
		beforeRead.size() + 1 == afterRead.size()
		beforeWrite.size() == afterWrite.size()
		testDashPerms.isEmpty()
	}

	void "signup invitation can be granted and revoked of permissions just like normal users"() {
		expect:
		!service.getPermissionsTo(streamOwned).find { it.invite == invite }

		when:
		service.systemGrant(invite, streamOwned, Permission.Operation.STREAM_GET)
		then:
		service.getPermissionsTo(streamOwned).find { it.invite == invite }

		when:
		service.systemRevoke(invite, streamOwned, Permission.Operation.STREAM_GET)
		then:
		!service.getPermissionsTo(streamOwned).find { it.invite == invite }
	}

	void "signup invitations are converted correctly"() {
		expect:
		!service.check(anotherUser, streamOwned, Permission.Operation.STREAM_GET)

		when: "pretend anotherUser was just created"
		service.systemGrant(invite, streamOwned, Permission.Operation.STREAM_GET)
		def permissions = service.transferInvitePermissionsTo(anotherUser)
		permissions*.save(flush: true) // flush hibernate cache
		then:
		service.check(anotherUser, streamOwned, Permission.Operation.STREAM_GET)
	}

	void "cannot revoke only share permission"() {
		setup:
		service.systemGrant(me, stream, Permission.Operation.STREAM_EDIT)
		service.systemGrant(me, stream, Permission.Operation.STREAM_GET)
		service.systemGrant(me, stream, Permission.Operation.STREAM_SHARE)

		when:
		service.systemRevoke(me, stream, Permission.Operation.STREAM_SHARE)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${stream}"
	}

	void "check anonymous permission"() {
		when:
		service.systemGrantAnonymousAccess(anonymousStream, Permission.Operation.STREAM_GET)
		then:
		service.checkAnonymousAccess(anonymousStream, Permission.Operation.STREAM_GET) == true

		when:
		service.systemRevokeAnonymousAccess(anonymousStream, Permission.Operation.STREAM_GET)
		then:
		service.checkAnonymousAccess(anonymousStream, Permission.Operation.STREAM_GET) == false
	}
}
