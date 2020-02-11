package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.api.NotPermittedException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
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
@Mock([SecUser, Key, SignupInvite, Module, Permission, Dashboard, Canvas])
class PermissionServiceSpec extends BeanMockingSpecification {

	SecUser me, anotherUser, stranger
	Key myKey, anotherUserKey, anonymousKey
	SignupInvite invite

	Dashboard dashAllowed, dashRestricted, dashOwned, dashPublic
	Permission dashReadPermission, dashAnonymousReadPermission

	StreamService streamService

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
		dashReadPermission = service.systemGrant(me, dashAllowed, Operation.DASHBOARD_GET)
		dashAnonymousReadPermission = service.systemGrantAnonymousAccess(dashPublic, Operation.DASHBOARD_GET)
		service.systemGrant(anonymousKey, dashAllowed, Operation.DASHBOARD_GET)

		streamService = mockBean(StreamService, Mock(StreamService))
    }

	void "test setup"() {
		expect:
		SecUser.count() == 3
		Key.count() == 3
		Dashboard.count() == 4
		Canvas.count() == 1
		Permission.count() == 29

		and: "anotherUser has an invitation"
		invite.username == anotherUser.username
	}

	void "access denied to non-permitted Dashboard"() {
		expect:
		!service.canReadDashboard(me, dashRestricted)
	}

	void "access granted through key to permitted Dashboard"() {
		expect:
		service.canReadDashboard(myKey, dashAllowed)
	}

	void "access denied through key to non-permitted Dashboard"() {
		expect:
		!service.canReadDashboard(myKey, dashRestricted)
	}

	void "access granted through anonymous key to permitted Dashboard"() {
		expect:
		service.canReadDashboard(anonymousKey, dashAllowed)
	}

	void "access denied through anonymous key to non-permitted Dashboard"() {
		expect:
		!service.canReadDashboard(anonymousKey, dashRestricted)
	}

	void "non-permitted third-parties have no access to resources"() {
		expect:
		!service.canReadDashboard(stranger, dashAllowed)
		!service.canReadDashboard(stranger, dashRestricted)
		!service.canReadDashboard(stranger, dashOwned)
	}

	void "canRead returns false on bad inputs"() {
		expect:
		!service.canReadDashboard(null, dashAllowed)
		!service.canReadDashboard(me, new Dashboard())
		!service.canReadDashboard(me, null)
	}

	void "canRead returns false on bad inputs using keys"() {
		expect:
		!service.canReadDashboard(null, dashAllowed)
		!service.canReadDashboard(myKey, new Dashboard())
		!service.canReadDashboard(anonymousKey, new Dashboard())
		!service.canReadDashboard(myKey, null)
	}

	void "getPermissionsTo returns all permissions for the given resource"() {
		setup:
		def perm = service.systemGrant(stranger, dashOwned, Operation.DASHBOARD_GET)
		expect:
		service.getPermissionsTo(dashOwned).size() == 6
		service.getPermissionsTo(dashOwned).contains(perm)
		service.getPermissionsTo(dashAllowed).size() == 7
		service.getPermissionsTo(dashAllowed).contains(dashReadPermission)
		service.getPermissionsTo(dashRestricted).size() == 5
		service.getPermissionsTo(dashRestricted)[0].user == anotherUser
	}

	void "getPermissionsTo with Operation returns all permissions for the given resource"() {
		setup:
		List<Permission> beforeRead = service.getPermissionsTo(dashOwned, Operation.DASHBOARD_GET)
		List<Permission> beforeWrite = service.getPermissionsTo(dashOwned, Operation.DASHBOARD_EDIT)
		Permission perm = service.systemGrant(stranger, dashOwned, Operation.DASHBOARD_GET)
		List<Permission> afterRead = service.getPermissionsTo(dashOwned, Operation.DASHBOARD_GET)
		List<Permission> afterWrite = service.getPermissionsTo(dashOwned, Operation.DASHBOARD_EDIT)
		List<Permission> all = service.getPermissionsTo(dashOwned)
		List<Permission> allOperations = new ArrayList<Permission>()
		Operation.values().collect { Operation op ->
			allOperations.addAll(service.getPermissionsTo(dashOwned, op))
		}
		expect:
		!beforeRead.contains(perm)
		afterRead.contains(perm)
		beforeRead.size() + 1 == afterRead.size()
		beforeWrite.size() == afterWrite.size()
		all.size() == allOperations.size()
	}

	void "getNonExpiredPermissionsTo with Operation returns all non-expired permissions for the given resource"() {
		Dashboard testDash = new Dashboard(id: "testdash", name:"testdash").save(validate:false)
		// craft an expired permission
		service.systemGrant(me, testDash, Operation.DASHBOARD_EDIT, null, new Date(0))
		setup:
		List<Permission> beforeRead = service.getNonExpiredPermissionsTo(dashOwned, Operation.DASHBOARD_GET)
		List<Permission> beforeWrite = service.getNonExpiredPermissionsTo(dashOwned, Operation.DASHBOARD_EDIT)
		Permission perm = service.systemGrant(stranger, dashOwned, Operation.DASHBOARD_GET)
		List<Permission> afterRead = service.getNonExpiredPermissionsTo(dashOwned, Operation.DASHBOARD_GET)
		List<Permission> afterWrite = service.getNonExpiredPermissionsTo(dashOwned, Operation.DASHBOARD_EDIT)
		List<Permission> testDashPerms = service.getNonExpiredPermissionsTo(testDash, Operation.DASHBOARD_EDIT)
		expect:
		!beforeRead.contains(perm)
		afterRead.contains(perm)
		beforeRead.size() + 1 == afterRead.size()
		beforeWrite.size() == afterWrite.size()
		testDashPerms.isEmpty()
	}

	void "getPermissionsTo(resource, userish) returns permissions for single user"() {
		expect:
		service.getPermissionsTo(dashOwned, me).size() == Operation.dashboardOperations().size()
		service.getPermissionsTo(dashOwned, anotherUser) == []
		service.getPermissionsTo(dashOwned, stranger) == []
		service.getPermissionsTo(dashOwned, null) == []
		service.getPermissionsTo(dashAllowed, me)[0].operation == Operation.DASHBOARD_GET
		service.getPermissionsTo(dashAllowed, anotherUser).size() == 5
		service.getPermissionsTo(dashAllowed, stranger) == []
		service.getPermissionsTo(dashAllowed, null) == []
		service.getPermissionsTo(dashRestricted, me) == []
		service.getPermissionsTo(dashRestricted, anotherUser).size() == 5
		service.getPermissionsTo(dashRestricted, stranger) == []
		service.getPermissionsTo(dashRestricted, null) == []
		service.getPermissionsTo(dashPublic, me)[0].operation == Operation.DASHBOARD_GET
		service.getPermissionsTo(dashPublic, anotherUser).size() == 6
		service.getPermissionsTo(dashPublic, stranger)[0].operation == Operation.DASHBOARD_GET
		service.getPermissionsTo(dashPublic, null)[0].operation == Operation.DASHBOARD_GET
	}

	void "getPermissionsTo(resource, userish) returns permissions for key"() {
		expect:
		service.getPermissionsTo(dashOwned, myKey).size() == Operation.dashboardOperations().size()
		service.getPermissionsTo(dashOwned, anotherUserKey) == []
		service.getPermissionsTo(dashOwned, anonymousKey) == []
		service.getPermissionsTo(dashAllowed, myKey)[0].operation == Operation.DASHBOARD_GET
		service.getPermissionsTo(dashAllowed, anotherUserKey).size() == 5
		service.getPermissionsTo(dashAllowed, anonymousKey)[0].operation == Operation.DASHBOARD_GET
		service.getPermissionsTo(dashRestricted, myKey) == []
		service.getPermissionsTo(dashRestricted, anotherUserKey).size() == 5
		service.getPermissionsTo(dashRestricted, anonymousKey) == []
		service.getPermissionsTo(dashPublic, myKey)[0].operation == Operation.DASHBOARD_GET
		service.getPermissionsTo(dashPublic, anotherUserKey).size() == 6
		service.getPermissionsTo(dashPublic, anonymousKey)[0].operation == Operation.DASHBOARD_GET
	}

	void "get throws exceptions on invalid resource"() {
		when:
		service.get(java.lang.Object, me, Permission.Operation.READ)
		then:
		thrown(IllegalArgumentException)

		when:
		service.get(null, me, Permission.Operation.READ)
		then:
		thrown(NullPointerException)
	}

	void "grant and revoke throw for non-'share'-access users"() {
		when:
		service.grant(me, dashAllowed, stranger, Permission.Operation.READ)
		then:
		thrown AccessControlException

		when:
		service.revoke(stranger, dashRestricted, me, Permission.Operation.READ)
		then:
		thrown AccessControlException
	}

	void "systemGrant() on an Ethereum user and a stream creates also inbox permissions"() {
		SecUser publisher1 = new SecUser()
		publisher1.id = 4L
		SecUser publisher2 = new SecUser()
		publisher2.id = 5L
		SecUser publisher3 = new SecUser()
		publisher3.id = 6L
		SecUser subscriber = new SecUser(username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)


		Stream pub1Inbox = new Stream(name: "publisher1", inbox: true)
		pub1Inbox.id = "publisher1"
		pub1Inbox.save(failOnError: true, validate: false)
		Stream pub2Inbox = new Stream(name: "publisher2", inbox: true)
		pub2Inbox.id = "publisher2"
		pub2Inbox.save(failOnError: true, validate: false)
		Stream pub3Inbox = new Stream(name: "publisher3", inbox: true)
		pub3Inbox.id = "publisher3"
		pub3Inbox.save(failOnError: true, validate: false)
		Stream subInbox = new Stream(name: subscriber.username, inbox: true)
		subInbox.id = subscriber.username
		subInbox.save(failOnError: true, validate: false)

		Stream stream = new Stream()
		stream.id = "stream"
		setup:
		service.systemGrant(publisher1, stream, Operation.STREAM_PUBLISH)
		service.systemGrant(publisher2, stream, Operation.STREAM_PUBLISH)

		when:
		// adding a new subscriber
		service.systemGrant(subscriber, stream, Operation.STREAM_SUBSCRIBE)
		// adding a new publisher
		service.systemGrant(publisher3, stream, Operation.STREAM_PUBLISH)
		then:
		2 * streamService.getInboxStreams([subscriber]) >> [subInbox]
		1 * streamService.getInboxStreams([publisher1, publisher2]) >> [pub1Inbox, pub2Inbox]
		1 * streamService.getInboxStreams([publisher3]) >> [pub3Inbox]
		// assertions after adding a new subscriber
		service.check(subscriber, pub1Inbox, Permission.Operation.STREAM_PUBLISH)
		service.check(subscriber, pub2Inbox, Permission.Operation.STREAM_PUBLISH)
		service.check(publisher1, subInbox, Permission.Operation.STREAM_PUBLISH)
		service.check(publisher2, subInbox, Permission.Operation.STREAM_PUBLISH)
		// assertions after adding a new publisher
		service.check(subscriber, pub3Inbox, Permission.Operation.STREAM_PUBLISH)
		service.check(publisher3, subInbox, Permission.Operation.STREAM_PUBLISH)
	}

	void "inbox stream permissions also work when anonymous keys have permissions to the stream"() {
		SecUser subscriber = new SecUser(username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		Key anonKey = new Key()
		anonKey.id = 1L
		anonKey.save(failOnError: true, validate: false)
		Stream stream = new Stream()
		stream.id = "stream"
		setup:
		service.systemGrant(anonKey, stream, Operation.STREAM_EDIT)
		when:
		service.systemGrant(subscriber, stream, Operation.STREAM_GET)
		then:
		noExceptionThrown()
	}

	void "cannot revoke only share permission"() {
		setup: "transfer effective 'ownership'"
		service.systemGrantAll(anotherUser, dashOwned)
		service.systemRevoke(me, dashOwned, Operation.DASHBOARD_SHARE)

		when:
		service.systemRevoke(anotherUser, dashOwned, Operation.DASHBOARD_SHARE)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${dashOwned}"
	}

	void "systemRevoke() on a stream also revokes the associated inbox permissions"() {
		SecUser publisher = new SecUser()
		publisher.id = 7L
		Stream pubInbox = new Stream(name: "publisher", inbox: true)
		pubInbox.id = "publisher"
		pubInbox.save(failOnError: true, validate: false)
		SecUser subscriber = new SecUser(username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		Stream subInbox = new Stream(name: subscriber.username, inbox: true)
		subInbox.id = subscriber.username
		subInbox.save(failOnError: true, validate: false)
		Stream stream = new Stream()
		stream.id = "stream"
		setup:
		new Permission(user: publisher, stream: stream, operation: Operation.WRITE).save(failOnError: true, validate: false)

		Permission parent = new Permission(user: subscriber, stream: stream, operation: Operation.READ).save(failOnError: true, validate: false)
		new Permission(user: subscriber, stream: pubInbox, operation: Operation.WRITE, parent: parent).save(failOnError: true, validate: false)
		new Permission(user: publisher, stream: subInbox, operation: Operation.WRITE, parent: parent).save(failOnError: true, validate: false)
		when:
		service.systemRevoke(subscriber, stream, Operation.READ)
		then:
		!service.canRead(subscriber, stream)
		!service.canWrite(subscriber, pubInbox)
		!service.canWrite(publisher, subInbox)
	}

	void "inbox permissions are maintained after systemRevoke() on a stream if there is another stream with permissions"() {
		SecUser publisher = new SecUser()
		publisher.id = 7L
		Stream pubInbox = new Stream(name: "publisher", inbox: true)
		pubInbox.id = "publisher"
		pubInbox.save(failOnError: true, validate: false)
		SecUser subscriber = new SecUser(username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		Stream subInbox = new Stream(name: subscriber.username, inbox: true)
		subInbox.id = subscriber.username
		subInbox.save(failOnError: true, validate: false)
		Stream stream1 = new Stream()
		stream1.id = "stream1"
		Stream stream2 = new Stream()
		stream2.id = "stream2"
		setup:
		new Permission(user: publisher, stream: stream1, operation: Operation.WRITE).save(failOnError: true, validate: false)

		Permission parent1 = new Permission(user: subscriber, stream: stream1, operation: Operation.READ).save(failOnError: true, validate: false)
		new Permission(user: subscriber, stream: pubInbox, operation: Operation.WRITE, parent: parent1).save(failOnError: true, validate: false)
		new Permission(user: publisher, stream: subInbox, operation: Operation.WRITE, parent: parent1).save(failOnError: true, validate: false)

		new Permission(user: publisher, stream: stream2, operation: Operation.WRITE).save(failOnError: true, validate: false)

		Permission parent2 = new Permission(user: subscriber, stream: stream2, operation: Operation.READ).save(failOnError: true, validate: false)
		new Permission(user: subscriber, stream: pubInbox, operation: Operation.WRITE, parent: parent2).save(failOnError: true, validate: false)
		new Permission(user: publisher, stream: subInbox, operation: Operation.WRITE, parent: parent2).save(failOnError: true, validate: false)
		when:
		service.systemRevoke(subscriber, stream1, Operation.READ)
		then:
		!service.canRead(subscriber, stream1)
		service.canRead(subscriber, stream2)
		service.canWrite(subscriber, pubInbox)
		service.canWrite(publisher, subInbox)
	}

	void "cannot revoke only share permission (via cascading READ)"() {
		setup: "transfer effective 'ownership'"
		service.systemGrantAll(anotherUser, dashOwned)
		service.systemRevoke(me, dashOwned, Operation.DASHBOARD_SHARE)

		when:
		service.systemRevoke(anotherUser, dashOwned, Operation.DASHBOARD_GET)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${dashOwned}"
	}

	void "cannot revoke only share permission (via cascading WRITE)"() {
		setup: "transfer effective 'ownership'"
		service.systemGrantAll(anotherUser, dashOwned)
		service.systemRevoke(me, dashOwned, Operation.DASHBOARD_SHARE)

		when:
		service.systemRevoke(anotherUser, dashOwned, Operation.DASHBOARD_EDIT)
		then:
		def e = thrown(AccessControlException)
		e.message == "Cannot revoke only SHARE permission of ${dashOwned}"
	}

	void "signup invitation can be granted and revoked of permissions just like normal users"() {
		expect:
		!service.getPermissionsTo(dashOwned).find { it.invite == invite }

		when:
		service.systemGrant(invite, dashOwned, Operation.DASHBOARD_GET)
		then:
		service.getPermissionsTo(dashOwned).find { it.invite == invite }

		when:
		service.systemRevoke(invite, dashOwned, Operation.DASHBOARD_GET)
		then:
		!service.getPermissionsTo(dashOwned).find { it.invite == invite }
	}

	void "signup invitations are converted correctly"() {
		expect:
		!service.canReadDashboard(anotherUser, dashOwned)

		when: "pretend anotherUser was just created"
		service.systemGrant(invite, dashOwned, Operation.DASHBOARD_GET)
		service.transferInvitePermissionsTo(anotherUser)
		then:
		service.canReadDashboard(anotherUser, dashOwned)
	}

	void "stranger can read public resources with anonymous read access"() {
		expect: "... but not more than read"
		service.canReadDashboard(stranger, dashPublic)
		!service.canWriteDashboard(stranger, dashPublic)
		!service.canShareDashboard(stranger, dashPublic)
	}

	void "verify does not throw if permission exists"() {
		when:
		service.verify(stranger, dashPublic, Operation.DASHBOARD_GET)
		then:
		notThrown(NotPermittedException)
	}

	void "verify throws if permission does not exist"() {
		when:
		service.verify(stranger, dashPublic, Operation.DASHBOARD_EDIT)
		then:
		def e = thrown(NotPermittedException)
		e.message == "stranger does not have permission to dashboard_edit Dashboard (id 4)"
	}

	void "systemRevokeAnonymousAccess() revokes anonymous access on a resource"() {
		assert Permission.exists(dashAnonymousReadPermission.id)
		assert service.canReadDashboard(null, dashPublic)

		when:
		service.systemRevokeAnonymousAccess(dashPublic, Operation.DASHBOARD_GET)

		then:
		!Permission.exists(dashAnonymousReadPermission.id)
		!service.canReadDashboard(null, dashPublic)
	}

	void "check() returns false if permission with endsAt set in past"() {
		def p = service.systemGrant(stranger, dashOwned, Operation.DASHBOARD_GET)
		p.endsAt = new Date(0)
		p.save(failOnError: true)

		expect:
		!service.check(stranger, dashOwned, Operation.DASHBOARD_GET)
	}

	void "check() returns true if permission with endsAt set in future"() {
		def p = service.systemGrant(stranger, dashOwned, Operation.DASHBOARD_GET)
		p.endsAt = new Date(System.currentTimeMillis() + 60000)
		p.save(failOnError: true)

		expect:
		service.check(stranger, dashOwned, Operation.DASHBOARD_GET)
	}

	void "cleanUpExpiredPermissions() deletes permissions that already ended"() {
		SecUser testUser = new SecUser(username: "testUser", password: "foo").save(validate:false)
		Stream testStream = new Stream(name: "testStream")
		testStream.id = "testStream"
		testStream.save(validate: false)

		assert Permission.findAllByStream(testStream).size() == 0

		when:
		Permission p1 = service.systemGrant(testUser, testStream, Operation.STREAM_GET)
		p1.endsAt = new Date(0)
		p1.save(failOnError: true)
		Permission p2 = service.systemGrant(testUser, testStream, Operation.STREAM_EDIT)
		p2.endsAt = new Date(System.currentTimeMillis() + 60000)
		p2.save(failOnError: true)

		then:
		Permission.findAllByStream(testStream).size() == 2

		when:
		service.cleanUpExpiredPermissions()

		then:
		Permission.findAllByStream(testStream).size() == 1
		!service.canReadStream(testUser, testStream)
		service.canWriteStream(testUser, testStream)
	}
}
