package com.unifina.service

import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV31
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.ExampleType
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem

import com.unifina.domain.data.Stream
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.feed.AbstractStreamListener

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.context.ApplicationContext
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(StreamService)
@Mock([Canvas, Dashboard, DashboardItem, Stream, SecUser, Key, IntegrationKey, Permission, PermissionService])
class StreamServiceSpec extends Specification {

	DashboardService dashboardService = Mock(DashboardService)

	SecUser me = new SecUser(username: "me")

	def setup() {
		// Setup application context
		def applicationContext = Stub(ApplicationContext) {
			getBean(DashboardService) >> dashboardService
		}

		// Setup grailsApplication
		def grailsApplication = new DefaultGrailsApplication()
		grailsApplication.setMainContext(applicationContext)

		service.grailsApplication = grailsApplication
		me.save(validate: false, failOnError: true)
	}

	def "getStream for inbox stream is case-insensitive"() {
		Stream s = new Stream()
		s.id = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		s.inbox = true
		s.name = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		s.save(validate: false)

		when:
		Stream queried1 = service.getStream(s.id)
		Stream queried2 = service.getStream("0x" + s.id.substring(2).toUpperCase())

		then:
		queried1.id == s.id
		queried2.id == s.id
	}

	def "add example shared streams"() {
		setup:
		service.permissionService = Mock(PermissionService)
		List<Stream> streams = []
		Stream s0 = new Stream(
			name: "example stream",
			exampleType: ExampleType.SHARE
		).save(failOnError: true)
		streams << s0
		Stream s1 = new Stream(
			name: "example 2 stream",
			exampleType: ExampleType.SHARE
		).save(failOnError: true)
		streams << s1

		when:
		service.addExampleStreams(me, streams)
		then:
		1 * service.permissionService.systemGrant(me, s0, Permission.Operation.READ)
		1 * service.permissionService.systemGrant(me, s1, Permission.Operation.READ)
	}

	void "createStream replaces empty name with default value"() {
		when:
		Stream s = service.createStream([name: ""], me)

		then:
		s.name == "Untitled Stream"
	}

	void "createStream results in persisted Stream"() {
		when:
		service.createStream([name: "name"], me)

		then:
		Stream.count() == 1
		Stream.list().first().name == "name"
	}

	void "createStream results in all permissions for Stream"() {
		when:
		def stream = service.createStream([name: "name"], me)

		then:
		Permission.findAllByStream(stream)*.toMap() == [
			[id: 1, user: "me", operation: "read"],
			[id: 2, user: "me", operation: "write"],
			[id: 3, user: "me", operation: "share"],
		]
	}

	void "createStream uses its params"() {
		when:
		def params = [
				name       : "Test stream",
				description: "Test stream",
				config     : [
						fields: [
								[name: "profit", type: "number"],
								[name: "keyword", type: "string"]
						]
				],
				requireSignedData: "true"
		]
		service.createStream(params, me)

		then: "stream is created"
		Stream.count() == 1
		def stream = Stream.findAll().get(0)
		stream.name == "Test stream"
		stream.description == "Test stream"
		stream.requireSignedData
	}

	void "getReadAuthorizedStream throws NotFoundException and does not invoke callback, if streamId doesn't exist"() {
		def cb = Mock(Closure)
		when:
		service.getReadAuthorizedStream("streamId", null, null, cb)
		then:
		thrown(NotFoundException)
		0 * cb._
	}

	void "getReadAuthorizedStream throws NotPermittedException and does not invoke callback, if not permitted to read stream through user"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)

		SecUser user = new SecUser(username: "username")
		user.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", user, null, cb)
		then:
		thrown(NotPermittedException)
		1 * service.permissionService.canRead(user, stream) >> false
		0 * cb._
	}

	void "getReadAuthorizedStream throws NotPermittedException and does not invoke callback, if not permitted to read stream through key"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)

		Key key = new Key(name: "name")
		key.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", null, key, cb)
		then:
		thrown(NotPermittedException)
		1 * service.permissionService.canRead(key, stream) >> false
		0 * cb._
	}

	void "getReadAuthorizedStream invokes callback if permitted to read stream through key"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)

		Key key = new Key(name: "name")
		key.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", null, key, cb)
		then:
		1 * service.permissionService.canRead(key, stream) >> true
		1 * cb.call(stream)
	}

	void "getReadAuthorizedStream invokes callback if permitted to read stream through user"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)

		SecUser user = new SecUser(username: "username")
		user.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", user, null, cb)
		then:
		1 * service.permissionService.canRead(user, stream) >> true
		1 * cb.call(stream)
	}

	void "getReadAuthorizedStream invokes callback if permitted to read stream anonymously (for public streams)"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", null, null, cb)
		then:
		1 * service.permissionService.canRead(null, stream) >> true
		1 * cb.call(stream)
	}

	void "getReadAuthorizedStream invokes callback if permitted to read (ui channel) stream indirectly through Canvas"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Canvas canvas = new Canvas(name: "canvas").save(failOnError: true, validate: false)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.uiChannel = true
		stream.uiChannelCanvas = canvas
		stream.save(failOnError: true, validate: false)

		SecUser user = new SecUser(username: "username")
		user.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", user, null, cb)
		then:
		1 * service.permissionService.canRead(user, stream) >> false
		1 * service.permissionService.canRead(user, canvas) >> true
		1 * cb.call(stream)
	}

	void "getReadAuthorizedStream throws NotPermittedException and does not invoke callback, if not permitted to read (ui channel) stream indirectly through Canvas"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Canvas canvas = new Canvas(name: "canvas").save(failOnError: true, validate: false)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.uiChannel = true
		stream.uiChannelCanvas = canvas
		stream.save(failOnError: true, validate: false)

		SecUser user = new SecUser(username: "username")
		user.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", user, null, cb)
		then:
		thrown(NotPermittedException)
		1 * service.permissionService.canRead(user, stream) >> false
		1 * service.permissionService.canRead(user, canvas) >> false
		0 * cb._
	}

	void "getReadAuthorizedStream invokes callback if permitted to read (ui channel) stream indirectly through Dashboard"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Canvas canvas = new Canvas(name: "canvas").save(failOnError: true, validate: false)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.uiChannel = true
		stream.uiChannelCanvas = canvas
		stream.uiChannelPath = "/canvases/notUsedId/modules/3"
		stream.save(failOnError: true, validate: false)

		SecUser user = new SecUser(username: "username")
		user.save(failOnError: true, validate: false)


		Dashboard dashboard = new Dashboard(name: "dashboard", user: user).save(failOnError: true, validate: false)
		DashboardItem dashboardItem = new DashboardItem(
				title: "dashboardItem",
				canvas: canvas,
				module: 3,
				webcomponent: "webcomponent"
		)
		dashboard.addToItems(dashboardItem)
		dashboard.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", user, null, cb)
		then:
		1 * service.permissionService.canRead(user, stream) >> false
		1 * service.permissionService.canRead(user, canvas) >> false
		1 * dashboardService.authorizedGetDashboardItem(dashboard.id, dashboardItem.id, user, Permission.Operation.READ) >> dashboardItem
		1 * cb.call(stream)
	}

	void "getReadAuthorizedStream throws NotPermittedException and does not invoke callback, if not permitted to read (ui channel) stream indirectly through Dashboard"() {
		def cb = Mock(Closure)
		service.permissionService = Mock(PermissionService)

		Canvas canvas = new Canvas(name: "canvas").save(failOnError: true, validate: false)

		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.uiChannel = true
		stream.uiChannelCanvas = canvas
		stream.uiChannelPath = "/canvases/notUsedId/modules/3"
		stream.save(failOnError: true, validate: false)

		SecUser user = new SecUser(username: "username")
		user.save(failOnError: true, validate: false)

		Dashboard dashboard = new Dashboard( name: "dashboard").save(failOnError: true, validate: false)
		DashboardItem dashboardItem = new DashboardItem(
				title: "dashboardItem",
				canvas: canvas,
				module: 3,
				webcomponent: "webcomponent"
		)
		dashboard.addToItems(dashboardItem)
		dashboard.save(failOnError: true, validate: false)

		when:
		service.getReadAuthorizedStream("streamId", user, null, cb)
		then:
		thrown(NotPermittedException)
		1 * service.permissionService.canRead(user, stream) >> false
		1 * service.permissionService.canRead(user, canvas) >> false
		1 * dashboardService.authorizedGetDashboardItem(dashboard.id, dashboardItem.id, user, Permission.Operation.READ) >> null
		0 * cb._
	}

	void "getStreamEthereumPublishers should return Ethereum addresses of users with write permission to the Stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		SecUser user1 = new SecUser(id: 1, username: "u1").save(failOnError: true, validate: false)
		IntegrationKey key1 = new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		SecUser user2 = new SecUser(id: 2, username: "u2").save(failOnError: true, validate: false)
		IntegrationKey key2 = new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		SecUser user3 = new SecUser(id: 3, username: "u3").save(failOnError: true, validate: false)

		// User with key but no write permission - this key should not be returned by the query
		SecUser userWithKeyButNoPermission = new SecUser(id: 4, username: "u4").save(failOnError: true, validate: false)
		new IntegrationKey(user: userWithKeyButNoPermission, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x12345e3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)

		Set<String> validAddresses = new HashSet<String>()
		validAddresses.add(key1.idInService)
		validAddresses.add(key2.idInService)
		Permission p1 = new Permission(user: user1)
		Permission p2 = new Permission(user: user2)
		Permission p3 = new Permission(user: user3)
		List<Permission> perms = [p1, p2, p3]
		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)
		when:
		Set<String> addresses = service.getStreamEthereumPublishers(stream)
		then:
		1 * service.permissionService.getPermissionsTo(stream, Permission.Operation.WRITE) >> perms
		addresses == validAddresses
	}

	void "isStreamEthereumPublisher should return true iff user has write permission to the stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		SecUser user1 = new SecUser(id: 1, username: "u1").save(failOnError: true, validate: false)
		String address1 = "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57"
		new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: address1).save(failOnError: true, validate: false)
		SecUser user2 = new SecUser(id: 2, username: "u2").save(failOnError: true, validate: false)
		String address2 = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: address2).save(failOnError: true, validate: false)
		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)
		when:
		boolean result1 = service.isStreamEthereumPublisher(stream, address1)
		// ensuring lookup doesn't depend on case
		boolean result1b = service.isStreamEthereumPublisher(stream, address1.toUpperCase())
		boolean result2 = service.isStreamEthereumPublisher(stream, address2)
		// ensuring lookup doesn't depend on case
		boolean result2b = service.isStreamEthereumPublisher(stream, address2.toUpperCase())
		then:
		2 * service.permissionService.canWrite(user1, stream) >> true
		result1 && result1b
		2 * service.permissionService.canWrite(user2, stream) >> false
		!result2 && !result2b
	}

	void "getStreamEthereumSubscribers should return Ethereum addresses of users with read permission to the Stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		SecUser user1 = new SecUser(id: 1, username: "u1").save(failOnError: true, validate: false)
		IntegrationKey key1 = new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		SecUser user2 = new SecUser(id: 2, username: "u2").save(failOnError: true, validate: false)
		IntegrationKey key2 = new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		SecUser user3 = new SecUser(id: 3, username: "u3").save(failOnError: true, validate: false)

		// User with key but no read permission - this key should not be returned by the query
		SecUser userWithKeyButNoPermission = new SecUser(id: 4, username: "u4").save(failOnError: true, validate: false)
		new IntegrationKey(user: userWithKeyButNoPermission, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x12345e3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)

		Set<String> validAddresses = new HashSet<String>()
		validAddresses.add(key1.idInService)
		validAddresses.add(key2.idInService)
		Permission p1 = new Permission(user: user1)
		Permission p2 = new Permission(user: user2)
		Permission p3 = new Permission(user: user3)
		List<Permission> perms = [p1, p2, p3]
		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)
		when:
		Set<String> addresses = service.getStreamEthereumSubscribers(stream)
		then:
		1 * service.permissionService.getPermissionsTo(stream, Permission.Operation.READ) >> perms
		addresses == validAddresses
	}

	void "isStreamEthereumSubscriber should return true iff user has read permission to the stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		SecUser user1 = new SecUser(id: 1, username: "u1").save(failOnError: true, validate: false)
		String address1 = "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57"
		new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: address1).save(failOnError: true, validate: false)
		SecUser user2 = new SecUser(id: 2, username: "u2").save(failOnError: true, validate: false)
		String address2 = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: address2).save(failOnError: true, validate: false)
		Stream stream = new Stream(name: "name")
		stream.id = "streamId"
		stream.save(failOnError: true, validate: false)
		when:
		boolean result1 = service.isStreamEthereumSubscriber(stream, address1)
		boolean result1b = service.isStreamEthereumSubscriber(stream, address1.toUpperCase())
		boolean result2 = service.isStreamEthereumSubscriber(stream, address2)
		boolean result2b = service.isStreamEthereumSubscriber(stream, address2.toUpperCase())
		then:
		2 * service.permissionService.canRead(user1, stream) >> true
		result1 && result1b
		2 * service.permissionService.canRead(user2, stream) >> false
		!result2 && !result2b
	}

	void "getInboxStreams() returns all inbox streams of the users"() {
		SecUser user1 = new SecUser(id: 1, username: "u1").save(failOnError: true, validate: false)
		IntegrationKey key1 = new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		SecUser user2 = new SecUser(id: 2, username: "u2").save(failOnError: true, validate: false)
		IntegrationKey key2 = new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		IntegrationKey key3 = new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: "0xfff1ae3f5efe8a01eca8c25933c32702cf4b1121").save(failOnError: true, validate: false)
		SecUser user3 = new SecUser(id: 3, username: "u3").save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: key1.idInService, inbox: true)
		s1.id = key1.idInService
		s1.save(failOnError: true, validate: false)
		Stream s2 = new Stream(name: key1.idInService, inbox: true)
		s2.id = key2.idInService
		s2.save(failOnError: true, validate: false)
		Stream s3 = new Stream(name: key1.idInService, inbox: true)
		s3.id = key3.idInService
		s3.save(failOnError: true, validate: false)

		Set<Stream> expectedResults = [s1, s2, s3]
		when:
		Set<Stream> results = service.getInboxStreams([user1, user2, user3])
		then:
		results == expectedResults
	}

	void "status ok and has recent messages"() {
		setup:
		service.cassandraService = Mock(CassandraService)
		Stream s = new Stream([name: "Stream 1", inactivityThresholdHours: 48])
		s.id = "s1"

		Date now = newDate(2019, 1, 15, 11, 12, 06)
		Date timestamp = newDate(2019, 1, 14, 11, 12, 06)
		long expected = timestamp.getTime()
		StreamMessage msg = new StreamMessageV31("s1", 0, timestamp.getTime(), 0L, "publisherId", "1", 0L, 0L,
			StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE, "", StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, "")

		when:
		StreamService.StreamStatus status = service.status(s, now)

		then:
		1 * service.cassandraService.getLatestFromAllPartitions(s) >> msg
		status.ok == true
		status.date.getTime() == expected
	}

	void "status not ok, no messages in stream"() {
		setup:
		service.cassandraService = Mock(CassandraService)
		Stream s = new Stream([name: "Stream 1", inactivityThresholdHours: 48])
		s.id = "s1"

		when:
		StreamService.StreamStatus status = service.status(s, new Date())

		then:
		1 * service.cassandraService.getLatestFromAllPartitions(s) >> null
		status.ok == false
		status.date == null
	}

	void "status stream has messages, but stream is stale"() {
		setup:
		service.cassandraService = Mock(CassandraService)
		Stream s = new Stream([name: "Stream 1", inactivityThresholdHours: 48])
		s.id = "s1"

		Date timestamp = newDate(2019, 1, 10, 12, 12, 06)
		long expected = timestamp.getTime()
		StreamMessage msg = new StreamMessageV31("s1", 0, timestamp.getTime(), 0L, "publisherId", "1", 0L, 0L,
			StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE, "", StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, "")
		Date now = newDate(2019, 1, 15, 0, 0, 0)

		when:
		StreamService.StreamStatus status = service.status(s, now)

		then:
		1 * service.cassandraService.getLatestFromAllPartitions(s) >> msg
		status.ok == false
		status.date.getTime() == expected
	}

	void "status inactivity threshold hours is zero and stream has messages"() {
		setup:
		service.cassandraService = Mock(CassandraService)
		Stream s = new Stream([name: "Stream 1", inactivityThresholdHours: 0])
		s.id = "s1"

		Date timestamp = new Date()
		long expected = timestamp.getTime()
		StreamMessage msg = new StreamMessageV31("s1", 0, timestamp.getTime(), 0L, "publisherId", "1", 0L, 0L,
			StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE, "", StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, "")

		when:
		StreamService.StreamStatus status = service.status(s, new Date())

		then:
		1 * service.cassandraService.getLatestFromAllPartitions(s) >> msg
		status.ok == true
		status.date.getTime() == expected
	}

	void "status inactivity threshold hours is zero and stream has no messages"() {
		setup:
		service.cassandraService = Mock(CassandraService)
		Stream s = new Stream([name: "Stream 1", inactivityThresholdHours: 0])
		s.id = "s1"

		when:
		StreamService.StreamStatus status = service.status(s, new Date())

		then:
		1 * service.cassandraService.getLatestFromAllPartitions(s) >> null
		status.ok == true
		status.date == null
	}

	Date newDate(int year, int month, int date, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance()
		cal.set(Calendar.YEAR, year)
		cal.set(Calendar.MONTH, month - 1)
		cal.set(Calendar.DATE, date)
		cal.set(Calendar.HOUR_OF_DAY, hour)
		cal.set(Calendar.MINUTE, minute)
		cal.set(Calendar.SECOND, second)
		return cal.getTime()
	}
}
