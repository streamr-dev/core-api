package com.unifina.service

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
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

	SecUser me, anotherUser, stranger, someone
	Key myKey, anotherUserKey, anonymousKey

	Dashboard dashAllowed, dashRestricted, dashOwned, dashPublic
	Permission dashReadPermission, dashAnonymousReadPermission

	void setup() {
		service = Holders.getApplicationContext().getBean(PermissionService)

		SecUser.findByUsername("me-permission-service-integration-spec@streamr.com")?.delete(flush: true)
		SecUser.findByUsername("him-permission-service-integration-spec@streamr.com")?.delete(flush: true)
		SecUser.findByUsername("stranger-permission-service-integration-spec@streamr.com")?.delete(flush: true)
		SecUser.findByUsername("someone-service-integration-spec@streamr.com")?.delete(flush: true)

		// Users
		me = new SecUser(
			username: "me-permission-service-integration-spec@streamr.com",
			name: "me",
			password: "foo",
			timezone: "Europe/Helsinki",
		).save(failOnError: true)

		anotherUser = new SecUser(
			username: "him-permission-service-integration-spec@streamr.com",
			name: "him",
			password: "bar",
			timezone: "Europe/Helsinki",
		).save(failOnError: true)

		stranger = new SecUser(
			username: "stranger-permission-service-integration-spec@streamr.com",
			name: "stranger",
			password: "x",
			timezone: "Europe/Helsinki",
		).save(failOnError: true)

		someone = new SecUser(
			username: "someone-service-integration-spec@streamr.com",
			name: "someone",
			password: "x",
			timezone: "Europe/Helsinki",
		).save(failOnError: true)

		// Keys
		myKey = new Key(name: "my key", user: me).save(failOnError: true)
		anotherUserKey = new Key(name: "another user's key", user: anotherUser).save(failOnError: true)
		anonymousKey = new Key(name: "anonymous key 1").save(failOnError: true)

		// Dashboards
		dashAllowed = new Dashboard(name:"allowed").save(failOnError: true)
		dashRestricted = new Dashboard(name:"restricted").save(failOnError: true)
		dashOwned = new Dashboard(name:"owned").save(failOnError: true)
		dashPublic = new Dashboard(name:"public").save(failOnError: true)

		service.systemGrantAll(anotherUser, dashAllowed)
		service.systemGrantAll(me, dashOwned)
		service.systemGrantAll(anotherUser, dashRestricted)
		service.systemGrantAll(anotherUser, dashPublic)

		// Set up the Permissions to the allowed resources
		dashReadPermission = service.grant(anotherUser, dashAllowed, me, Permission.Operation.READ)
		dashAnonymousReadPermission = service.grantAnonymousAccess(anotherUser, dashPublic)
		service.grant(anotherUser, dashAllowed, anonymousKey)
	}

	void cleanup() {
		Permission.findAllByDashboard(dashAllowed)*.delete(flush: true)
		Permission.findAllByDashboard(dashRestricted)*.delete(flush: true)
		Permission.findAllByDashboard(dashOwned)*.delete(flush: true)
		Permission.findAllByDashboard(dashPublic)*.delete(flush: true)

		dashAllowed?.delete(flush: true)
		dashRestricted?.delete(flush: true)
		dashOwned?.delete(flush: true)
		dashPublic?.delete(flush: true)

		myKey?.delete(flush: true)
		anotherUserKey?.delete(flush: true)
		anonymousKey?.delete(flush: true)

		me?.delete(flush: true)
		anotherUser?.delete(flush: true)
		stranger?.delete(flush: true)
		someone?.delete(flush: true)
	}


	void "get closure filtering works as expected"() {
		expect:
		service.get(Dashboard, me) { like("name", "%ll%") } == [dashAllowed]
		service.get(Dashboard, me, Permission.Operation.SHARE) == [dashOwned]
		service.get(Dashboard, me, Permission.Operation.SHARE) { like("name", "%ll%") } == []
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.READ)
		service.grant(me, dashOwned, stranger, Permission.Operation.SHARE)
		then:
		service.get(Dashboard, stranger) == [dashOwned]
		service.get(Dashboard, stranger, Permission.Operation.SHARE) == [dashOwned]

		expect:
		!(dashOwned in service.get(Dashboard, anotherUser))

		when: "stranger shares read access"
		service.grant(stranger, dashOwned, anotherUser)
		then:
		dashOwned in service.get(Dashboard, anotherUser)
		!(dashOwned in service.get(Dashboard, anotherUser, Permission.Operation.SHARE))

		when:
		service.revoke(stranger, dashOwned, anotherUser)
		then:
		!(dashOwned in service.get(Dashboard, anotherUser))

		when: "of course, it's silly to revoke 'share' access since it might already been re-shared..."
		service.revoke(me, dashOwned, stranger)
		service.grant(stranger, dashOwned, anotherUser)
		then:
		thrown AccessControlException
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, dashOwned, stranger, Permission.Operation.READ)
		service.grant(me, dashOwned, stranger, Permission.Operation.SHARE)
		when:
		service.revoke(me, dashOwned, stranger)
		then: "by default, revoke all access"
		service.get(Dashboard, stranger) == []
		service.get(Dashboard, stranger, Permission.Operation.SHARE) == []
	}

	void "revocation is granular"() {
		setup:
		service.grant(me, dashOwned, stranger, Permission.Operation.READ)
		service.grant(me, dashOwned, stranger, Permission.Operation.SHARE)
		when:
		service.revoke(me, dashOwned, stranger, Permission.Operation.SHARE)
		then: "only 'share' access is revoked"
		service.get(Dashboard, stranger) == [dashOwned]
		service.get(Dashboard, stranger, Permission.Operation.SHARE) == []
	}

	void "get does not return expired permissions"() {
		def p1 = service.systemGrant(someone, dashRestricted, Permission.Operation.READ)
		def p2 = service.systemGrant(someone, dashOwned, Permission.Operation.READ)
		p1.endsAt = new Date(System.currentTimeMillis() - 1000*60000)
		p2.endsAt = new Date(0)
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		expect:
		service.get(Dashboard, someone, Permission.Operation.READ) == []
	}

	void "get returns non-expired permissions"() {
		def p1 = service.systemGrant(someone, dashRestricted, Permission.Operation.READ)
		def p2 = service.systemGrant(someone, dashOwned, Permission.Operation.READ)
		p1.endsAt = new Date(System.currentTimeMillis() + 10000)
		p2.endsAt = new Date(System.currentTimeMillis() + 1000000)
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		expect:
		service.get(Dashboard, someone, Permission.Operation.READ) as Set == [dashOwned, dashRestricted] as Set
	}

	void "getAll lists public resources"() {
		expect:
		service.getAll(Dashboard, me) as Set == [dashOwned, dashPublic, dashAllowed] as Set
		service.getAll(Dashboard, anotherUser) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.getAll(Dashboard, stranger) == [dashPublic]
	}

	void "getAll lists public resources with keys"() {
		expect:
		service.getAll(Dashboard, myKey) as Set == [dashOwned, dashPublic, dashAllowed] as Set
		service.getAll(Dashboard, anotherUserKey) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.getAll(Dashboard, anonymousKey) as Set == [dashPublic, dashAllowed] as Set
	}

	void "getAll returns public resources on bad/null user"() {
		expect:
		service.get(Dashboard, new SecUser()) == []
		service.get(Dashboard, null) == []
		service.getAll(Dashboard, new SecUser()) == [dashPublic]
		service.getAll(Dashboard, null) == [dashPublic]
	}

	void "granting and revoking read rights"() {
		when:
		service.grant(me, dashOwned, stranger)
		then:
		service.get(Dashboard, stranger) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger)
		then:
		service.get(Dashboard, stranger) == []
	}

	void "granting and revoking write rights"() {
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.WRITE)
		then:
		service.get(Dashboard, stranger, Permission.Operation.WRITE) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger, Permission.Operation.WRITE)
		then:
		service.get(Dashboard, stranger, Permission.Operation.WRITE) == []

		when: "revoking read also revokes write"
		service.grant(me, dashOwned, stranger, Permission.Operation.WRITE)
		service.revoke(me, dashOwned, stranger)
		then:
		service.get(Dashboard, stranger, Permission.Operation.WRITE) == []
	}

	void "granting and revoking share rights"() {
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.SHARE)
		then:
		service.get(Dashboard, stranger, Permission.Operation.SHARE) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger, Permission.Operation.SHARE)
		then:
		service.get(Dashboard, stranger, Permission.Operation.SHARE) == []

		when: "revoking read also revokes share"
		service.grant(me, dashOwned, stranger, Permission.Operation.SHARE)
		service.revoke(me, dashOwned, stranger)
		then:
		service.get(Dashboard, stranger, Permission.Operation.SHARE) == []
	}

	void "granting works (roughly) idempotently"() {
		expect:
		service.get(Dashboard, stranger) == []
		when: "double-granting still has the same effect: there exists a permission for user to resource"
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		then: "now you see it..."
		service.get(Dashboard, stranger) == [dashOwned]
		when:
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		service.revoke(me, dashOwned, stranger)
		then: "now you don't."
		service.get(Dashboard, stranger) == []
	}

	void "retrieve all readable Dashboards correctly"() {
		expect:
		service.get(Dashboard, me) as Set == [dashOwned, dashAllowed] as Set
		service.get(Dashboard, anotherUser) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.get(Dashboard, stranger) == []
	}

	void "retrieve all readable Dashboards correctly with keys"() {
		expect:
		service.get(Dashboard, myKey) as Set == [dashOwned, dashAllowed] as Set
		service.get(Dashboard, anotherUserKey) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.get(Dashboard, anonymousKey) as Set == [dashAllowed] as Set
	}
}
