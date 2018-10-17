package com.unifina.service

import com.unifina.api.NotPermittedException
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
		dashAllowed = new Dashboard(id: "allowed", name:"allowed").save(validate:false)
		dashRestricted = new Dashboard(id: "restricted", name:"restricted").save(validate:false)
		dashOwned = new Dashboard(id: "owned", name:"owned").save(validate:false)
		dashPublic = new Dashboard(id: "public", name:"public").save(validate:false)

		service.systemGrantAll(anotherUser, dashAllowed)
		service.systemGrantAll(me, dashOwned)
		service.systemGrantAll(anotherUser, dashRestricted)
		service.systemGrantAll(anotherUser, dashPublic)

		Canvas canvas = new Canvas().save(validate: false)
		service.systemGrantAll(anotherUser, canvas)

		// Set up the Permissions to the allowed resources
		dashReadPermission = service.grant(anotherUser, dashAllowed, me, Operation.READ)
		dashAnonymousReadPermission = service.grantAnonymousAccess(anotherUser, dashPublic)
		service.grant(anotherUser, dashAllowed, anonymousKey)
    }

	void "test setup"() {
		expect:
		SecUser.count() == 3
		Key.count() == 3
		Dashboard.count() == 4
		Canvas.count() == 1
		Permission.count() == 18

		and: "anotherUser has an invitation"
		invite.username == anotherUser.username
	}

	void "access granted to permitted Dashboard"() {
		expect:
		service.canRead(me, dashAllowed)
		service.verifyRead(me, dashAllowed)
	}

	void "access denied to non-permitted Dashboard"() {
		expect:
		!service.canRead(me, dashRestricted)
	}

	void "access granted through key to permitted Dashboard"() {
		expect:
		service.canRead(myKey, dashAllowed)
	}

	void "access denied through key to non-permitted Dashboard"() {
		expect:
		!service.canRead(myKey, dashRestricted)
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
		service.getPermissionsTo(dashOwned).contains(perm)
		service.getPermissionsTo(dashAllowed).size() == 5
		service.getPermissionsTo(dashAllowed).contains(dashReadPermission)
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

	void "get throws exceptions on invalid resource"() {
		when:
		service.get(java.lang.Object, me)
		then:
		thrown(IllegalArgumentException)

		when:
		service.get(null, me)
		then:
		thrown(NullPointerException)
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
	}

	void "cannot revoke only share permission"() {
		setup: "transfer effective 'ownership'"
		service.systemGrantAll(anotherUser, dashOwned)
		service.systemRevoke(me, dashOwned, Operation.SHARE)

		when:
		service.systemRevoke(anotherUser, dashOwned, Operation.SHARE)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${dashOwned}"
	}

	void "cannot revoke only share permission (via cascading READ)"() {
		setup: "transfer effective 'ownership'"
		service.systemGrantAll(anotherUser, dashOwned)
		service.systemRevoke(me, dashOwned, Operation.SHARE)

		when:
		service.systemRevoke(anotherUser, dashOwned, Operation.READ)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${dashOwned}"
	}

	void "cannot revoke only share permission (via cascading WRITE)"() {
		setup: "transfer effective 'ownership'"
		service.systemGrantAll(anotherUser, dashOwned)
		service.systemRevoke(me, dashOwned, Operation.SHARE)

		when:
		service.systemRevoke(anotherUser, dashOwned, Operation.WRITE)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${dashOwned}"
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

	void "verify does not throw if permission exists"() {
		when:
		service.verify(stranger, dashPublic, Operation.READ)
		then:
		notThrown(NotPermittedException)
	}

	void "verify throws if permission does not exist"() {
		when:
		service.verify(stranger, dashPublic, Operation.WRITE)
		then:
		def e = thrown(NotPermittedException)
		e.message == "stranger does not have permission to write Dashboard (id 4)"
	}

	void "systemRevokeAnonymousAccess() revokes anonymous access on a resource"() {
		assert Permission.exists(dashAnonymousReadPermission.id)
		assert service.canRead(null, dashPublic)

		when:
		service.systemRevokeAnonymousAccess(dashPublic)

		then:
		!Permission.exists(dashAnonymousReadPermission.id)
		!service.canRead(null, dashPublic)
	}

	void "check() returns false if permission with endsAt set in past"() {
		def p = service.systemGrant(stranger, dashOwned, Operation.READ)
		p.endsAt = new Date(0)
		p.save(failOnError: true)

		expect:
		!service.check(stranger, dashOwned, Operation.READ)
	}

	void "check() returns true if permission with endsAt set in future"() {
		def p = service.systemGrant(stranger, dashOwned, Operation.READ)
		p.endsAt = new Date(System.currentTimeMillis() + 60000)
		p.save(failOnError: true)

		expect:
		service.check(stranger, dashOwned, Operation.READ)
	}
}
