package com.unifina.service

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.IdGenerator
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

	Canvas canvas
	// User has indirect permissions to this UI channel stream via the canvas
	Stream stream

	Dashboard dashboard
	// User has indirect permissions to this UI channel stream via the dashboard
	Stream uiChannelStream
	Canvas uiChannelCanvas

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
		).save(failOnError: true)

		anotherUser = new SecUser(
			username: "him-permission-service-integration-spec@streamr.com",
			name: "him",
			password: "bar",
		).save(failOnError: true)

		stranger = new SecUser(
			username: "stranger-permission-service-integration-spec@streamr.com",
			name: "stranger",
			password: "x",
		).save(failOnError: true)

		someone = new SecUser(
			username: "someone-service-integration-spec@streamr.com",
			name: "someone",
			password: "x",
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
		dashReadPermission = service.systemGrant(me, dashAllowed, Permission.Operation.DASHBOARD_GET)
		dashAnonymousReadPermission = service.systemGrantAnonymousAccess(dashPublic, Permission.Operation.DASHBOARD_GET)
		service.grant(anotherUser, dashAllowed, anonymousKey, Permission.Operation.DASHBOARD_GET)

		canvas = new Canvas().save(validate: true, failOnError: true)
		stream = new Stream(name: "ui channel", uiChannel: true, uiChannelCanvas: canvas, uiChannelPath: "/canvases/" + canvas.id + "/modules/2")
		stream.id = "stream-id"
		stream.save(validate: true, failOnError: true)

		uiChannelCanvas = new Canvas()
		uiChannelCanvas.save(validate: true, failOnError: true)
		dashboard = new Dashboard(name: "dashboard")
		dashboard.save(validate: true, failOnError: true)
		uiChannelStream = new Stream(name: "ui channel", uiChannel: true, uiChannelCanvas: uiChannelCanvas, uiChannelPath: "/canvases/" + uiChannelCanvas.id + "/modules/2")
		uiChannelStream.id = "stream-id-2"
		uiChannelStream.save(validate: true, failOnError: true)
	}

	void cleanup() {
		Permission.findAllByDashboard(dashAllowed)*.delete(flush: true)
		Permission.findAllByDashboard(dashRestricted)*.delete(flush: true)
		Permission.findAllByDashboard(dashOwned)*.delete(flush: true)
		Permission.findAllByDashboard(dashPublic)*.delete(flush: true)
		Permission.findAllByDashboard(dashboard)*.delete(flush: true)
		Permission.findAllByCanvas(canvas)*.delete(flush: true)
		Permission.findAllByCanvas(uiChannelCanvas)*.delete(flush: true)
		Permission.findAllByStream(stream)*.delete(flush: true)
		Permission.findAllByStream(uiChannelStream)*.delete(flush: true)

		dashAllowed?.delete(flush: true)
		dashRestricted?.delete(flush: true)
		dashOwned?.delete(flush: true)
		dashPublic?.delete(flush: true)
		dashboard?.delete(flush: true)

		myKey?.delete(flush: true)
		anotherUserKey?.delete(flush: true)
		anonymousKey?.delete(flush: true)

		me?.delete(flush: true)
		anotherUser?.delete(flush: true)
		stranger?.delete(flush: true)
		someone?.delete(flush: true)

		stream?.delete(flush: true)
		uiChannelStream?.delete(flush: true)
		canvas?.delete(flush: true)
		uiChannelCanvas?.delete(flush: true)
	}

	void "get closure filtering works as expected"() {
		expect:
		service.get(Dashboard, me, Permission.Operation.DASHBOARD_GET) { like("name", "%ll%") } == [dashAllowed]
		service.get(Dashboard, me, Permission.Operation.DASHBOARD_SHARE) == [dashOwned]
		service.get(Dashboard, me, Permission.Operation.DASHBOARD_SHARE) { like("name", "%ll%") } == []
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_SHARE)
		then:
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == [dashOwned]
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_SHARE) == [dashOwned]

		expect:
		!(dashOwned in service.get(Dashboard, anotherUser, Permission.Operation.DASHBOARD_GET))

		when: "stranger shares read access"
		service.grant(stranger, dashOwned, anotherUser, Permission.Operation.DASHBOARD_GET)
		then:
		dashOwned in service.get(Dashboard, anotherUser, Permission.Operation.DASHBOARD_GET)
		!(dashOwned in service.get(Dashboard, anotherUser, Permission.Operation.DASHBOARD_SHARE))

		when:
		service.revoke(stranger, dashOwned, anotherUser, Permission.Operation.DASHBOARD_GET)
		then:
		!(dashOwned in service.get(Dashboard, anotherUser, Permission.Operation.DASHBOARD_GET))

		when: "of course, it's silly to revoke 'dashboard_share' access since it might already been re-shared..."
		service.revoke(me, dashOwned, stranger, Permission.Operation.DASHBOARD_SHARE)
		service.grant(stranger, dashOwned, anotherUser, Permission.Operation.DASHBOARD_SHARE)
		then:
		thrown AccessControlException
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_SHARE)
		when:
		service.revoke(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		then: "by default, revoke all access"
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == []
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_SHARE) == [dashOwned]
	}

	void "revocation is granular"() {
		setup:
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_SHARE)
		when:
		service.revoke(me, dashOwned, stranger, Permission.Operation.DASHBOARD_SHARE)
		then: "only 'share' access is revoked"
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == [dashOwned]
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_SHARE) == []
	}

	void "get does not return expired permissions"() {
		def p1 = service.systemGrant(someone, dashRestricted, Permission.Operation.DASHBOARD_GET)
		def p2 = service.systemGrant(someone, dashOwned, Permission.Operation.DASHBOARD_GET)
		p1.endsAt = new Date(System.currentTimeMillis() - 1000*60000)
		p2.endsAt = new Date(0)
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		expect:
		service.get(Dashboard, someone, Permission.Operation.DASHBOARD_GET) == []
	}

	void "get returns non-expired permissions"() {
		def p1 = service.systemGrant(someone, dashRestricted, Permission.Operation.DASHBOARD_GET)
		def p2 = service.systemGrant(someone, dashOwned, Permission.Operation.DASHBOARD_GET)
		p1.endsAt = new Date(System.currentTimeMillis() + 10000)
		p2.endsAt = new Date(System.currentTimeMillis() + 1000000)
		p1.save(failOnError: true)
		p2.save(failOnError: true)

		expect:
		service.get(Dashboard, someone, Permission.Operation.DASHBOARD_GET) as Set == [dashOwned, dashRestricted] as Set
	}

	void "getAll lists public resources"() {
		expect:
		service.getAll(Dashboard, me, Permission.Operation.DASHBOARD_GET) as Set == [dashOwned, dashPublic, dashAllowed] as Set
		service.getAll(Dashboard, anotherUser, Permission.Operation.DASHBOARD_GET) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.getAll(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == [dashPublic]
	}

	void "getAll lists public resources with keys"() {
		expect:
		service.getAll(Dashboard, myKey, Permission.Operation.DASHBOARD_GET) as Set == [dashOwned, dashPublic, dashAllowed] as Set
		service.getAll(Dashboard, anotherUserKey, Permission.Operation.DASHBOARD_GET) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.getAll(Dashboard, anonymousKey, Permission.Operation.DASHBOARD_GET) as Set == [dashPublic, dashAllowed] as Set
	}

	void "getAll returns public resources on bad/null user"() {
		expect:
		service.get(Dashboard, new SecUser(), Permission.Operation.DASHBOARD_GET) == []
		service.get(Dashboard, null, Permission.Operation.DASHBOARD_GET) == []
		service.getAll(Dashboard, new SecUser(), Permission.Operation.DASHBOARD_GET) == [dashPublic]
		service.getAll(Dashboard, null, Permission.Operation.DASHBOARD_GET) == [dashPublic]
	}

	void "granting and revoking read rights"() {
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		then:
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		then:
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == []
	}

	void "granting and revoking share rights"() {
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_SHARE)
		then:
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_SHARE) == [dashOwned]
	}

	void "granting works (roughly) idempotently"() {
		expect:
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == []
		when: "double-granting still has the same effect: there exists a permission for user to resource"
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		then: "now you see it..."
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == [dashOwned]
		when:
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.grant(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		service.revoke(me, dashOwned, stranger, Permission.Operation.DASHBOARD_GET)
		then: "now you don't."
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == []
	}

	void "retrieve all readable Dashboards correctly"() {
		expect:
		service.get(Dashboard, me, Permission.Operation.DASHBOARD_GET) as Set == [dashOwned, dashAllowed] as Set
		service.get(Dashboard, anotherUser, Permission.Operation.DASHBOARD_GET) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.get(Dashboard, stranger, Permission.Operation.DASHBOARD_GET) == []
	}

	void "retrieve all readable Dashboards correctly with keys"() {
		expect:
		service.get(Dashboard, myKey, Permission.Operation.DASHBOARD_GET) as Set == [dashOwned, dashAllowed] as Set
		service.get(Dashboard, anotherUserKey, Permission.Operation.DASHBOARD_GET) as Set == [dashAllowed, dashRestricted, dashPublic] as Set
		service.get(Dashboard, anonymousKey, Permission.Operation.DASHBOARD_GET) as Set == [dashAllowed] as Set
	}

	void "getPermissionsTo(resource, userish) returns correct UI channel permissions via associated canvas"() {
		service.systemGrantAll(me, canvas)

		expect:
		service.getPermissionsTo(stream, me).size() == 5
		service.check(me, uiChannelStream, Permission.Operation.CANVAS_GET)
		service.check(me, stream, Permission.Operation.STREAM_GET)
		service.check(me, stream, Permission.Operation.STREAM_PUBLISH)
		service.check(me, stream, Permission.Operation.STREAM_SUBSCRIBE)
		service.check(me, stream, Permission.Operation.STREAM_DELETE)
	}

	void "getPermissionsTo(resource, userish) returns correct UI channel read permissions via associated dashboard"() {
		service.systemGrantAll(me, dashboard)
		def permissions = service.getPermissionsTo(uiChannelStream, me)

		expect:
		permissions.size() == 1
		permissions.get(0).operation == Permission.Operation.CANVAS_GET
		service.check(me, uiChannelStream, Permission.Operation.DASHBOARD_GET)
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

	void "granting permissions results in correct number of inbox stream permissions"() {
		EthereumIntegrationKeyService ethereumIntegrationKeyService = Holders.grailsApplication.mainContext.getBean(EthereumIntegrationKeyService)
		List<SecUser> readers = (1..10).collect {
			ethereumIntegrationKeyService.createEthereumUser(ethereumIntegrationKeyService.generateAccount().getAddress())
		}
		List<SecUser> writers = (1..20).collect {
			ethereumIntegrationKeyService.createEthereumUser(ethereumIntegrationKeyService.generateAccount().getAddress())
		}
		Stream manyToManyStream = new Stream(name: "many-to-many stream")
		manyToManyStream.id = IdGenerator.getShort()
		manyToManyStream.save(validate: false)

		when:
		writers.each { service.systemGrant(it, manyToManyStream, Permission.Operation.STREAM_PUBLISH) }

		then:
		Permission.countByStream(manyToManyStream) == writers.size()

		when:
		readers.each { service.systemGrant(it, manyToManyStream, Permission.Operation.STREAM_SUBSCRIBE) }
		List<Permission> parentPermissions = Permission.findAllByStream(manyToManyStream)

		then:
		parentPermissions.size() == writers.size() + readers.size()
		Permission.countByParentInList(parentPermissions) == writers.size() * readers.size() * 2

		when: // there are expired subscriptions
		readers.each { service.systemGrant(it, manyToManyStream, Permission.Operation.STREAM_SUBSCRIBE, null, new Date(0)) }
		parentPermissions = Permission.findAllByStream(manyToManyStream)

		then: // expired permissions don't add to the permission bloat
		parentPermissions.size() == writers.size() + 2 * readers.size()
		Permission.countByParentInList(parentPermissions) == writers.size() * readers.size() * 2
	}
}
