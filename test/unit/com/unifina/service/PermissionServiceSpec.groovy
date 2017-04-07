package com.unifina.service

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import java.security.AccessControlException

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(PermissionService)
@Mock([SecUser, Key, SignupInvite, Module, ModulePackage, Permission, Dashboard, Canvas])
class PermissionServiceSpec extends Specification {

	SecUser me, anotherUser, stranger
	Key myKey, anotherUserKey, anonymousKey
	SignupInvite invite

	Dashboard dashAllowed, dashRestricted, dashOwned, dashPublic
	Permission dashReadPermission, dashAnonymousReadPermission

    def setup() {

		// Users
		me = new SecUser(username: "me", password: "foo").save(validate:false)
		anotherUser = new SecUser(username: "him", password: "bar").save(validate:false)
		stranger = new SecUser(username: "stranger", password: "x").save(validate:false)

		// Keys
		myKey = new Key(name: "my key", user: me).save(failOnError: true)
		anotherUserKey = new Key(name: "another user's key", user: anotherUser).save(failOnError: true)
		anonymousKey = new Key(name: "anonymous key 1").save(failOnError: true)

		// Sign-up invitations can also receive Permissions; they will later be converted to User permissions
		invite = new SignupInvite(username: "him", code: "sikritCode", sent: true, used: false).save(validate:false)

		// Dashboards
		dashAllowed = new Dashboard(name:"allowed", user:anotherUser).save(validate:false)
		dashRestricted = new Dashboard(name:"restricted", user:anotherUser).save(validate:false)
		dashOwned = new Dashboard(name:"owned", user:me).save(validate:false)
		dashPublic = new Dashboard(name:"public", user:anotherUser).save(validate:false)

		new Canvas(user: anotherUser).save(validate: false)

		// Set up the Permissions to the allowed resources
		dashReadPermission = service.grant(anotherUser, dashAllowed, me)
		dashAnonymousReadPermission = service.grantAnonymousAccess(anotherUser, dashPublic)
		service.grant(anotherUser, dashAllowed, anonymousKey)
    }

	void "test setup"() {
		expect:
		SecUser.count() == 3
		Key.count() == 3
		Dashboard.count() == 4
		Permission.count() == 3

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

	void "access granted through key to permitted Dashboard"() {
		expect:
		service.canRead(myKey, dashAllowed)
	}


	void "access denied through key to non-permitted Dashboard"() {
		expect:
		!service.canRead(myKey, dashRestricted)
	}

	void "access granted through key to own Dashboard"() {
		expect:
		service.canRead(myKey, dashOwned)
	}

	void "access granted through anonymous key to permitted Dashboard"() {
		expect:
		service.canRead(anonymousKey, dashAllowed)
	}

	void "access denies through anonymous key to non-permitted Dashboard"() {
		expect:
		!service.canRead(anonymousKey, dashRestricted)
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

	void "canRead returns false on bad inputs using keys"() {
		expect:
		!service.canRead(null, dashAllowed)
		!service.canRead(myKey, new Dashboard())
		!service.canRead(anonymousKey, new Dashboard())
		!service.canRead(myKey, null)
	}

	void "getPermissionsTo returns all permissions for the given resource"() {
		setup:
		def perm = service.grant(me, dashOwned, stranger, Operation.READ)
		expect:
		service.getPermissionsTo(dashOwned).size() == 4
		service.getPermissionsTo(dashOwned)[0] == perm
		service.getPermissionsTo(dashAllowed).size() == 5
		service.getPermissionsTo(dashAllowed)[0] == dashReadPermission
		service.getPermissionsTo(dashRestricted).size() == 3
		service.getPermissionsTo(dashRestricted)[0].user == anotherUser
	}

	void "getSingleUserPermissionsTo returns permissions for single user"() {
		expect:
		service.getPermissionsTo(dashOwned, me).size() == 3
		service.getPermissionsTo(dashOwned, anotherUser) == []
		service.getPermissionsTo(dashOwned, stranger) == []
		service.getPermissionsTo(dashOwned, null) == []
		service.getPermissionsTo(dashAllowed, me)[0].operation == Operation.READ
		service.getPermissionsTo(dashAllowed, anotherUser).size() == 3
		service.getPermissionsTo(dashAllowed, stranger) == []
		service.getPermissionsTo(dashAllowed, null) == []
		service.getPermissionsTo(dashRestricted, me) == []
		service.getPermissionsTo(dashRestricted, anotherUser).size() == 3
		service.getPermissionsTo(dashRestricted, stranger) == []
		service.getPermissionsTo(dashRestricted, null) == []
		service.getPermissionsTo(dashPublic, me)[0].operation == Operation.READ
		service.getPermissionsTo(dashPublic, anotherUser).size() == 4
		service.getPermissionsTo(dashPublic, stranger)[0].operation == Operation.READ
		service.getPermissionsTo(dashPublic, null)[0].operation == Operation.READ
	}

	void "getSingleUserPermissionsTo returns permissions for key"() {
		expect:
		service.getPermissionsTo(dashOwned, myKey).size() == 3
		service.getPermissionsTo(dashOwned, anotherUserKey) == []
		service.getPermissionsTo(dashOwned, anonymousKey) == []
		service.getPermissionsTo(dashAllowed, myKey)[0].operation == Operation.READ
		service.getPermissionsTo(dashAllowed, anotherUserKey).size() == 3
		service.getPermissionsTo(dashAllowed, anonymousKey)[0].operation == Operation.READ
		service.getPermissionsTo(dashRestricted, myKey) == []
		service.getPermissionsTo(dashRestricted, anotherUserKey).size() == 3
		service.getPermissionsTo(dashRestricted, anonymousKey) == []
		service.getPermissionsTo(dashPublic, myKey)[0].operation == Operation.READ
		service.getPermissionsTo(dashPublic, anotherUserKey).size() == 4
		service.getPermissionsTo(dashPublic, anonymousKey)[0].operation == Operation.READ
	}

	void "retrieve all readable Dashboards correctly"() {
		expect:
		service.get(Dashboard, me) == [dashOwned, dashAllowed]
		service.get(Dashboard, anotherUser) == [dashAllowed, dashRestricted, dashPublic]
		service.get(Dashboard, stranger) == []
	}

	void "retrieve all readable Dashboards correctly with keys"() {
		expect:
		service.get(Dashboard, myKey) == [dashOwned, dashAllowed]
		service.get(Dashboard, anotherUserKey) == [dashAllowed, dashRestricted, dashPublic]
		service.get(Dashboard, anonymousKey) == [dashAllowed]
	}

	void "getAll lists public resources"() {
		expect:
		service.getAll(Dashboard, me) == [dashOwned, dashPublic, dashAllowed]
		service.getAll(Dashboard, anotherUser) == [dashAllowed, dashRestricted, dashPublic]
		service.getAll(Dashboard, stranger) == [dashPublic]
	}

	void "getAll lists public resources with keys"() {
		expect:
		service.getAll(Dashboard, myKey) == [dashOwned, dashPublic, dashAllowed]
		service.getAll(Dashboard, anotherUserKey) == [dashAllowed, dashRestricted, dashPublic]
		service.getAll(Dashboard, anonymousKey) == [dashPublic, dashAllowed]
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
