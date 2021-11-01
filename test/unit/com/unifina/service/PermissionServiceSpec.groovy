package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.domain.*
import com.unifina.domain.Permission.Operation
import grails.gsp.PageRenderer
import grails.plugin.mail.MailService
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
@Mock([User, SignupInvite, Permission, Stream])
class PermissionServiceSpec extends BeanMockingSpecification {
	User me
	User anotherUser
	User stranger
	Stream streamAllowed
	Stream streamRestricted
	Stream streamOwned
	Stream streamPublic
	Permission streamAnonymousReadPermission

	StreamService streamService

	def setup() {
		// Users
		me = new User(username: "me").save(validate: false)
		anotherUser = new User(username: "him").save(validate: false)
		stranger = new User(username: "stranger").save(validate: false)

		// Streams
		streamAllowed = new Stream(name: "allowed")
		streamAllowed.id = "allowed"
		streamAllowed.save(validate: false)
		streamRestricted = new Stream(name: "restricted")
		streamRestricted.id = "restricted"
		streamRestricted.save(validate: false)
		streamOwned = new Stream(name: "owned")
		streamOwned.id = "owned"
		streamOwned.save(validate: false)
		streamPublic = new Stream(name: "public")
		streamPublic.id = "public"
		streamPublic.save(validate: false)

		service.systemGrantAll(anotherUser, streamAllowed)
		service.systemGrantAll(me, streamOwned)
		service.systemGrantAll(anotherUser, streamRestricted)
		service.systemGrantAll(anotherUser, streamPublic)

		// Set up the Permissions to the allowed resources
		streamAnonymousReadPermission = service.systemGrantAnonymousAccess(streamPublic, Operation.STREAM_GET)

		streamService = mockBean(StreamService, Mock(StreamService))
	}

	void "access denied to non-permitted Stream"() {
		expect:
		!service.check(me, streamRestricted, Permission.Operation.STREAM_GET)
	}

	void "non-permitted third-parties have no access to resources"() {
		expect:
		!service.check(stranger, streamAllowed, Permission.Operation.STREAM_GET)
		!service.check(stranger, streamRestricted, Permission.Operation.STREAM_GET)
		!service.check(stranger, streamOwned, Permission.Operation.STREAM_GET)
	}

	void "canRead returns false on bad inputs"() {
		expect:
		!service.check(null, streamAllowed, Permission.Operation.STREAM_GET)
		!service.check(me, new Stream(), Permission.Operation.STREAM_GET)
		!service.check(me, null, Permission.Operation.STREAM_GET)
	}

	void "getPermissionsTo(resource, userish) returns permissions for single user"() {
		expect:
		service.getPermissionsTo(streamOwned, anotherUser) == []
		service.getPermissionsTo(streamOwned, stranger) == []
		service.getPermissionsTo(streamOwned, null) == []
		service.getPermissionsTo(streamAllowed, anotherUser).size() == Operation.streamOperations().size()
		service.getPermissionsTo(streamAllowed, stranger) == []
		service.getPermissionsTo(streamAllowed, null) == []
		service.getPermissionsTo(streamRestricted, me) == []
		service.getPermissionsTo(streamRestricted, anotherUser).size() == Operation.streamOperations().size()
		service.getPermissionsTo(streamRestricted, stranger) == []
		service.getPermissionsTo(streamRestricted, null) == []
		service.getPermissionsTo(streamPublic, me)[0].operation == Operation.STREAM_GET
		service.getPermissionsTo(streamPublic, anotherUser).size() == Operation.streamOperations().size()
		service.getPermissionsTo(streamPublic, stranger)[0].operation == Operation.STREAM_GET
		service.getPermissionsTo(streamPublic, null)[0].operation == Operation.STREAM_GET
	}

	void "get throws exceptions on invalid resource"() {
		when:
		service.get(java.lang.Object, me, Permission.Operation.STREAM_GET)
		then:
		thrown(IllegalArgumentException)

		when:
		service.get(null, me, Permission.Operation.STREAM_GET)
		then:
		thrown(NullPointerException)
	}

	void "grant and revoke throw for non-'share'-access users"() {
		when:
		service.grant(me, streamAllowed, stranger, Permission.Operation.STREAM_GET)
		then:
		thrown AccessControlException

		when:
		service.revoke(stranger, streamRestricted, me, Permission.Operation.STREAM_GET)
		then:
		thrown AccessControlException
	}

	void "systemRevoke() on a stream also revokes the parent permissions"() {
		User publisher = new User()
		publisher.id = 7L
		Stream pub = new Stream(name: "publisher")
		pub.id = "publisher"
		pub.save(failOnError: true, validate: false)
		User subscriber = new User(username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		Stream sub = new Stream(name: subscriber.username)
		sub.id = subscriber.username
		sub.save(failOnError: true, validate: false)
		Stream stream = new Stream()
		stream.id = "stream"
		setup:
		new Permission(user: publisher, stream: stream, operation: Operation.STREAM_PUBLISH).save(failOnError: true, validate: false)

		Permission parent = new Permission(user: subscriber, stream: stream, operation: Operation.STREAM_SUBSCRIBE).save(failOnError: true, validate: false)
		new Permission(user: subscriber, stream: pub, operation: Operation.STREAM_PUBLISH, parent: parent).save(failOnError: true, validate: false)
		new Permission(user: publisher, stream: sub, operation: Operation.STREAM_PUBLISH, parent: parent).save(failOnError: true, validate: false)
		when:
		service.systemRevoke(subscriber, stream, Operation.STREAM_SUBSCRIBE)
		then:
		!service.check(subscriber, stream, Permission.Operation.STREAM_SUBSCRIBE)
		!service.check(subscriber, pub, Permission.Operation.STREAM_PUBLISH)
		!service.check(publisher, sub, Permission.Operation.STREAM_PUBLISH)
	}

	void "stranger can read public resources with anonymous read access"() {
		expect: "... but not more than read"
		service.check(stranger, streamPublic, Permission.Operation.STREAM_GET)
		!service.check(stranger, streamPublic, Permission.Operation.STREAM_EDIT)
		!service.check(stranger, streamPublic, Permission.Operation.STREAM_SHARE)
	}

	void "verify does not throw if permission exists"() {
		when:
		service.verify(stranger, streamPublic, Operation.STREAM_GET)
		then:
		notThrown(NotPermittedException)
	}

	void "verify throws if permission does not exist"() {
		when:
		service.verify(stranger, streamPublic, Operation.STREAM_EDIT)
		then:
		def e = thrown(NotPermittedException)
		e.message == "stranger does not have permission to stream_edit Stream (id public)"
	}

	void "systemRevokeAnonymousAccess() revokes anonymous access on a resource"() {
		assert Permission.exists(streamAnonymousReadPermission.id)
		assert service.check(null, streamPublic, Permission.Operation.STREAM_GET)

		when:
		service.systemRevokeAnonymousAccess(streamPublic, Operation.STREAM_GET)

		then:
		!Permission.exists(streamAnonymousReadPermission.id)
		!service.check(null, streamPublic, Permission.Operation.STREAM_GET)
	}

	void "check() returns false if permission with endsAt set in past"() {
		def p = service.systemGrant(stranger, streamOwned, Operation.STREAM_GET)
		p.endsAt = new Date(0)
		p.save(failOnError: true)

		expect:
		!service.check(stranger, streamOwned, Operation.STREAM_GET)
	}

	void "check() returns true if permission with endsAt set in future"() {
		def p = service.systemGrant(stranger, streamOwned, Operation.STREAM_GET)
		p.endsAt = new Date(System.currentTimeMillis() + 60000)
		p.save(failOnError: true)

		expect:
		service.check(stranger, streamOwned, Operation.STREAM_GET)
	}

	void "cleanUpExpiredPermissions() deletes permissions that already ended"() {
		User testUser = new User(username: "testUser").save(validate: false)
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
		!service.check(testUser, testStream, Operation.STREAM_GET)
		service.check(testUser, testStream, Permission.Operation.STREAM_EDIT)
	}

	Stream newStream(String id) {
		Stream s = new Stream(name: "Stream " + id)
		s.id = id
		return s.save(validate: true, failOnError: true, flush: true)
	}

	void "save sends an email for read permission"() {
		setup:
		service.mailService = Mock(MailService)
		service.groovyPageRenderer = Mock(PageRenderer)
		User me = new User(id: 1, username: "me@me.net").save(validate: false)
		User other = new User(id: 2, username: "permission@recipient.net").save(validate: false)
		Stream stream = newStream("own-id")
		Resource res = new Resource(Stream, stream.id)
		User apiUser = me
		Operation op = Operation.STREAM_GET
		String targetUsername = other.username
		String sharer = me.username
		String recipient = other.username
		String subjectTemplate = "%USER% wants to share a %RESOURCE% with you via Streamr Core"
		EmailMessage msg = new EmailMessage(sharer, recipient, subjectTemplate, res)
		service.systemGrant(me, stream, Operation.STREAM_SHARE)
		when:
		service.savePermissionAndSendShareResourceEmail(apiUser, op, targetUsername, msg)
		then:
		service.check(other, stream, Operation.STREAM_GET)
		1 * streamService.getStream(stream.id) >> stream
		1 * service.groovyPageRenderer.render(_) >> "<html>email</html>"
		1 * service.mailService.sendMail { _ }
	}

	void "save does not send an email for write permission"() {
		setup:
		service.mailService = Mock(MailService)
		service.groovyPageRenderer = Mock(PageRenderer)
		User me = new User(id: 1, username: "me@me.net").save(validate: false)
		User other = new User(id: 2, username: "permission@recipient.net").save(validate: false)
		Stream stream = newStream("own-id")
		Resource res = new Resource(Stream, stream.id)
		User apiUser = me
		Operation op = Operation.STREAM_EDIT
		String targetUsername = other.username
		String sharer = me.username
		String recipient = other.username
		String subjectTemplate = "%USER% wants to share a %RESOURCE% with you via Streamr Core"
		EmailMessage msg = new EmailMessage(sharer, recipient, subjectTemplate, res)
		service.systemGrant(me, stream, Operation.STREAM_SHARE)
		when:
		service.savePermissionAndSendShareResourceEmail(apiUser, op, targetUsername, msg)
		then:
		1 * streamService.getStream(stream.id) >> stream
		service.check(other, stream, Operation.STREAM_EDIT)
		0 * service.groovyPageRenderer.render(_) >> "<html>email</html>"
		0 * service.mailService.sendMail { _ }
	}

	void "save does not send an email for share permission"() {
		setup:
		service.mailService = Mock(MailService)
		service.groovyPageRenderer = Mock(PageRenderer)
		User me = new User(id: 1, username: "me@me.net").save(validate: false)
		User other = new User(id: 2, username: "permission@recipient.net").save(validate: false)
		Stream stream = newStream("own-id")
		Resource res = new Resource(Stream, stream.id)
		User apiUser = me
		Operation op = Operation.STREAM_SHARE
		String sharer = me.username
		String recipient = other.username
		String subjectTemplate = "%USER% wants to share a %RESOURCE% with you via Streamr Core"
		EmailMessage msg = new EmailMessage(sharer, recipient, subjectTemplate, res)
		String targetUsername = other.username
		service.systemGrant(me, stream, Operation.STREAM_SHARE)
		when:
		service.savePermissionAndSendShareResourceEmail(apiUser, op, targetUsername, msg)
		then:
		service.check(other, stream, Operation.STREAM_SHARE)
		1 * streamService.getStream(stream.id) >> stream
		0 * service.groovyPageRenderer.render(_) >> "<html>email</html>"
		0 * service.mailService.sendMail { _ }
	}

	void "save() creates a new user with permission if unknown ethereum address provided"() {
		setup:
		service.mailService = Mock(MailService)
		service.groovyPageRenderer = Mock(PageRenderer)
		EthereumUserService ethereumUserService = mockBean(EthereumUserService, Mock(EthereumUserService))
		String ethUserUsername = "0xa50E97f6a98dD992D9eCb8207c2Aa58F54970729"
		User createdEthUser = new User(username: ethUserUsername, name: "Ethereum User")
		createdEthUser.save(validate: true, failOnError: true)
		Stream stream = newStream("own-id")
		Resource res = new Resource(Stream, stream.id)
		User apiUser = me
		Operation op = Operation.STREAM_GET
		service.systemGrant(me, stream, Operation.STREAM_SHARE)
		when:
		service.savePermissionForEthereumAccount(ethUserUsername, apiUser, op, res, SignupMethod.UNKNOWN)
		then:
		1 * streamService.getStream(stream.id) >> stream
		1 * ethereumUserService.getOrCreateFromEthereumAddress(ethUserUsername, SignupMethod.UNKNOWN) >> createdEthUser
		0 * service.groovyPageRenderer.render(_) >> "<html>email</html>"
		0 * service.mailService.sendMail { _ }
		service.check(createdEthUser, stream, op)
	}

	void "save sends an email if the user has no account yet"() {
		setup:
		service.mailService = Mock(MailService)
		service.groovyPageRenderer = Mock(PageRenderer)
		service.signupCodeService = Mock(SignupCodeService)
		User me = new User(id: 1, username: "me@me.net").save(validate: false)
		User other = new User(id: 2, username: "permission@recipient.net").save(validate: false)
		Stream stream = newStream("own-id")
		Resource res = new Resource(Stream, stream.id)
		User apiUser = me
		Operation op = Operation.STREAM_GET
		String targetUsername = other.username
		String sharer = me.username
		String recipient = other.username
		String subjectTemplate = "%USER% wants to share a %RESOURCE% with you via Streamr Core"
		EmailMessage msg = new EmailMessage(sharer, recipient, subjectTemplate, res)
		SignupInvite invite = new SignupInvite(
			code: "x",
			email: recipient,
			used: false,
			sent: false,
		)
		service.systemGrant(apiUser, stream, Operation.STREAM_SHARE)
		when:
		service.savePermissionAndSendEmailShareResourceInvite(apiUser, recipient, op, msg)
		then:
		service.check(invite, stream, op)
		1 * streamService.getStream(stream.id) >> stream
		1 * service.signupCodeService.create(recipient) >> invite
		1 * service.groovyPageRenderer.render(_) >> "<html>email</html>"
		1 * service.mailService.sendMail { _ }
	}

	void "save anonymous permission"() {
		setup:
		User me = new User(id: 1, username: "me@me.net").save(validate: false)
		Stream stream = newStream("own-id")
		Resource res = new Resource(Stream, stream.id)
		User apiUser = me
		Operation op = Operation.STREAM_GET
		service.systemGrant(apiUser, stream, Operation.STREAM_SHARE)
		when:
		service.saveAnonymousPermission(apiUser, op, res)
		then:
		1 * streamService.getStream(stream.id) >> stream
		service.check(apiUser, stream, op)
	}

	void "findAllPermissions() won't show list of permissions without 'share' permission (string id)"() {
		setup:
		Stream stream = newStream("stream-id")
		Resource resource = new Resource(Stream, stream.id)
		User apiUser = me
		boolean subscriptions = false
		when:
		service.findAllPermissions(resource, apiUser, subscriptions)
		then:
		1 * streamService.getStream(stream.id) >> stream
		thrown NotPermittedException
	}

	void "findPermission finds permission"() {
		setup:
		Stream stream = newStream("stream-id")
		Resource resource = new Resource(Stream, stream.id)
		User apiUser = me
		service.systemGrant(apiUser, stream, Operation.STREAM_SHARE)
		Permission p = service.systemGrant(apiUser, stream, Operation.STREAM_EDIT)
		p.save(flush: true)
		when:
		1 * streamService.getStream(stream.id) >> stream
		Permission permission = service.findPermission(p.id, resource, apiUser)
		then:
		p == permission
	}

	void "findPermission throws NotFoundException when permission is not found "() {
		setup:
		Stream streamOwned = newStream("stream-id")
		Resource resource = new Resource(Stream, streamOwned.id)
		User apiUser = me
		service.systemGrant(apiUser, streamOwned, Operation.STREAM_SHARE)
		Permission p = service.systemGrant(apiUser, streamOwned, Operation.STREAM_DELETE)
		p.save(flush: true)
		when:
		Permission permission = service.findPermission(null, resource, apiUser)
		then:
		def e = thrown(NotFoundException)
		e.type == "Stream"
		e.id == "stream-id"
	}

	void "index won't show list of permissions without 'share' permission (Stream using id)"() {
		setup:
		Stream stream = newStream("stream-id")
		Resource resource = new Resource(Stream, stream.id)
		User apiUser = me
		boolean subscriptions = false
		when:
		service.findAllPermissions(resource, apiUser, subscriptions)
		then:
		1 * streamService.getStream(stream.id) >> stream
		thrown NotPermittedException
	}
}
