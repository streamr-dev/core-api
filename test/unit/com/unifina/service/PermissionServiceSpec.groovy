package com.unifina.service

import com.unifina.domain.security.SignupInvite
import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation

import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.dashboard.Dashboard

import com.unifina.utils.IdGenerator
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin

import java.security.AccessControlException

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(PermissionService)
@Mock([SecUser, SignupInvite, Module, ModulePackage, Permission, Dashboard, Canvas])
class PermissionServiceSpec extends Specification {

	SecUser me, anotherUser, stranger
	SignupInvite invite

	Dashboard dashAllowed, dashRestricted, dashOwned, dashPublic
	Permission dashReadPermission, dashAnonymousReadPermission

    def setup() {

		// Users
		me = new SecUser(username: "me", password: "foo", apiKey: "apiKey", apiSecret: "apiSecret").save(validate:false)
		anotherUser = new SecUser(username: "him", password: "bar", apiKey: "anotherApiKey", apiSecret: "anotherApiSecret").save(validate:false)
		stranger = new SecUser(username: "stranger", password: "x", apiKey: "strangeApiKey", apiSecret: "strangeApiSecret").save(validate:false)

		// Sign-up invitations can also receive Permissions; they will later be converted to User permissions
		invite = new SignupInvite(username: "him", code: "sikritCode", sent: true, used: false).save(validate:false)

		// Dashboards
		dashAllowed = new Dashboard(name:"allowed", user:anotherUser).save(validate:false)
		dashRestricted = new Dashboard(name:"restricted", user:anotherUser).save(validate:false)
		dashOwned = new Dashboard(name:"owned", user:me).save(validate:false)
		dashPublic = new Dashboard(name:"public", user:anotherUser).save(validate:false)

		def canvas = new Canvas(user: anotherUser).save(validate: false)

		// Set up the Permissions to the allowed resources
		dashReadPermission = service.grant(anotherUser, dashAllowed, me)
		dashAnonymousReadPermission = service.grantAnonymousAccess(anotherUser, dashPublic)
    }

	void "test setup"() {
		expect:
		SecUser.count() == 3
		Dashboard.count() == 4
		Permission.count() == 2

		and: "anotherUser has an invitation"
		invite.username == anotherUser.username
	}

	void "access granted to permitted Dashboard"() {
		expect:
		service.canRead(me, dashAllowed)
	}

	void "access denied to non-permitted Dashboard"() {
		expect:
		!service.canRead(me, dashRestricted)
	}

	void "access granted to own Dashboard"() {
		expect:
		service.canRead(me, dashOwned)
	}

	void "non-permitted third-parties have no access to resources"() {
		expect:
		!service.canRead(stranger, dashAllowed)
		!service.canRead(stranger, dashRestricted)
		!service.canRead(stranger, dashOwned)
	}

	void "canRead returns false on bad inputs"() {
		expect:
		!service.canRead(null, dashAllowed)
		!service.canRead(me, new Dashboard())
		!service.canRead(me, null)
	}

	void "getPermissionsTo returns all permissions for the given resource"() {
		setup:
		def perm = service.grant(me, dashOwned, stranger, Operation.READ)
		expect:
		service.getPermissionsTo(dashOwned).size() == 4
		service.getPermissionsTo(dashOwned)[0] == perm
		service.getPermissionsTo(dashAllowed).size() == 4
		service.getPermissionsTo(dashAllowed)[0] == dashReadPermission
		service.getPermissionsTo(dashRestricted).size() == 3
		service.getPermissionsTo(dashRestricted)[0].user == anotherUser
	}

	void "getSingleUserPermissionsTo returns permissions for single user"() {
		expect:
		service.getSingleUserPermissionsTo(dashOwned, me).size() == 3
		service.getSingleUserPermissionsTo(dashOwned, anotherUser) == []
		service.getSingleUserPermissionsTo(dashOwned, stranger) == []
		service.getSingleUserPermissionsTo(dashOwned, null) == []
		service.getSingleUserPermissionsTo(dashAllowed, me)[0].operation == Operation.READ
		service.getSingleUserPermissionsTo(dashAllowed, anotherUser).size() == 3
		service.getSingleUserPermissionsTo(dashAllowed, stranger) == []
		service.getSingleUserPermissionsTo(dashAllowed, null) == []
		service.getSingleUserPermissionsTo(dashRestricted, me) == []
		service.getSingleUserPermissionsTo(dashRestricted, anotherUser).size() == 3
		service.getSingleUserPermissionsTo(dashRestricted, stranger) == []
		service.getSingleUserPermissionsTo(dashRestricted, null) == []
		service.getSingleUserPermissionsTo(dashPublic, me)[0].operation == Operation.READ
		service.getSingleUserPermissionsTo(dashPublic, anotherUser).size() == 4
		service.getSingleUserPermissionsTo(dashPublic, stranger)[0].operation == Operation.READ
		service.getSingleUserPermissionsTo(dashPublic, null)[0].operation == Operation.READ
	}

	void "retrieve all readable Dashboards correctly"() {
		expect:
		service.get(Dashboard, me) == [dashOwned, dashAllowed]
		service.get(Dashboard, anotherUser) == [dashAllowed, dashRestricted, dashPublic]
		service.get(Dashboard, stranger) == []
	}

	void "getAll lists public resources"() {
		expect:
		service.getAll(Dashboard, me) == [dashOwned, dashAllowed, dashPublic]
		service.getAll(Dashboard, anotherUser) == [dashAllowed, dashRestricted, dashPublic]
		service.getAll(Dashboard, stranger) == [dashPublic]
	}

	void "get throws IllegalArgumentException on invalid resource"() {
		when:
		service.get(java.lang.Object, me)
		then:
		thrown IllegalArgumentException

		when:
		service.get(null, me)
		then:
		thrown IllegalArgumentException
	}

	void "getAll returns public resources on bad/null user"() {
		expect:
		service.get(Dashboard, new SecUser()) == []
		service.get(Dashboard, null) == []
		service.getAll(Dashboard, new SecUser()) == [dashPublic]
		service.getAll(Dashboard, null) == [dashPublic]
	}

	void "get closure filtering works as expected"() {
		expect:
		service.get(Dashboard, me) { like("name", "%ll%") } == [dashAllowed]
		service.get(Dashboard, me, Operation.SHARE) == [dashOwned]
		service.get(Dashboard, me, Operation.SHARE) { like("name", "%ll%") } == []
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
		service.grant(me, dashOwned, stranger, Operation.WRITE)
		then:
		service.get(Dashboard, stranger, Operation.WRITE) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger, Operation.WRITE)
		then:
		service.get(Dashboard, stranger, Operation.WRITE) == []

		when: "revoking read also revokes write"
		service.grant(me, dashOwned, stranger, Operation.WRITE)
		service.revoke(me, dashOwned, stranger)
		then:
		service.get(Dashboard, stranger, Operation.WRITE) == []
	}

	void "granting and revoking share rights"() {
		when:
		service.grant(me, dashOwned, stranger, Operation.SHARE)
		then:
		service.get(Dashboard, stranger, Operation.SHARE) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger, Operation.SHARE)
		then:
		service.get(Dashboard, stranger, Operation.SHARE) == []

		when: "revoking read also revokes share"
		service.grant(me, dashOwned, stranger, Operation.SHARE)
		service.revoke(me, dashOwned, stranger)
		then:
		service.get(Dashboard, stranger, Operation.SHARE) == []
	}

	void "grant and revoke throw for non-'share'-access users"() {
		when:
		service.grant(me, dashAllowed, stranger)
		then:
		thrown AccessControlException

		when:
		service.revoke(stranger, dashRestricted, me)
		then:
		thrown AccessControlException

		when:
		service.grant(anotherUser, dashAllowed, me, Operation.SHARE)
		service.revoke(me, dashAllowed, anotherUser)
		then: "Y U try to revoke owner's access?! That should never be generated from the UI!"
		thrown AccessControlException
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, dashOwned, stranger, Operation.READ)
		service.grant(me, dashOwned, stranger, Operation.SHARE)
		then:
		service.get(Dashboard, stranger) == [dashOwned]
		service.get(Dashboard, stranger, Operation.SHARE) == [dashOwned]

		expect:
		!(dashOwned in service.get(Dashboard, anotherUser))

		when: "stranger shares read access"
		service.grant(stranger, dashOwned, anotherUser)
		then:
		dashOwned in service.get(Dashboard, anotherUser)
		!(dashOwned in service.get(Dashboard, anotherUser, Operation.SHARE))

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

	void "revocation is granular"() {
		setup:
		service.grant(me, dashOwned, stranger, Operation.READ)
		service.grant(me, dashOwned, stranger, Operation.SHARE)
		when:
		service.revoke(me, dashOwned, stranger, Operation.SHARE)
		then: "only 'share' access is revoked"
		service.get(Dashboard, stranger) == [dashOwned]
		service.get(Dashboard, stranger, Operation.SHARE) == []
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, dashOwned, stranger, Operation.READ)
		service.grant(me, dashOwned, stranger, Operation.SHARE)
		when:
		service.revoke(me, dashOwned, stranger)
		then: "by default, revoke all access"
		service.get(Dashboard, stranger) == []
		service.get(Dashboard, stranger, Operation.SHARE) == []
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

	void "signup invitation can be granted and revoked of permissions just like normal users"() {
		expect:
		!service.getPermissionsTo(dashOwned).find { it.invite == invite }

		when:
		service.grant(me, dashOwned, invite)
		then:
		service.getPermissionsTo(dashOwned).find { it.invite == invite }

		when:
		service.revoke(me, dashOwned, invite)
		then:
		!service.getPermissionsTo(dashOwned).find { it.invite == invite }
	}

	void "signup invitations are converted correctly"() {
		expect:
		!service.canRead(anotherUser, dashOwned)

		when: "pretend anotherUser was just created"
		service.grant(me, dashOwned, invite)
		service.transferInvitePermissionsTo(anotherUser)
		then:
		service.canRead(anotherUser, dashOwned)
	}

	void "stranger can read public resources with anonymous read access"() {
		expect: "... but not more than read"
		service.canRead(stranger, dashPublic)
		!service.canWrite(stranger, dashPublic)
		!service.canShare(stranger, dashPublic)
	}

	void "check() can handle detached resource instances"() {
		Dashboard detached = new Dashboard()
		detached.id = dashOwned.id

		expect:
		service.check(me, detached, Operation.READ)
	}
}
