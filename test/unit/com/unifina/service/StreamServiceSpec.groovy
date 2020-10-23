package com.unifina.service


import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.domain.ExampleType
import com.unifina.domain.Dashboard
import com.unifina.domain.DashboardItem
import com.unifina.domain.Stream
import com.unifina.domain.IntegrationKey
import com.unifina.domain.Key
import com.unifina.domain.Permission
import com.unifina.domain.User
import com.unifina.domain.Canvas
import com.unifina.utils.TestUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@TestFor(StreamService)
@Mock([Canvas, Dashboard, DashboardItem, Stream, User, Key, IntegrationKey, Permission, PermissionService])
class StreamServiceSpec extends Specification {

	DashboardService dashboardService = Mock(DashboardService)

	User me = new User(username: "me")

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
		1 * service.permissionService.systemGrant(me, s0, Permission.Operation.STREAM_GET)
		1 * service.permissionService.systemGrant(me, s0, Permission.Operation.STREAM_SUBSCRIBE)
		1 * service.permissionService.systemGrant(me, s1, Permission.Operation.STREAM_GET)
		1 * service.permissionService.systemGrant(me, s1, Permission.Operation.STREAM_SUBSCRIBE)
	}

	void "createStream replaces empty name with stream id"() {
		when:
		Stream s = service.createStream(new CreateStreamCommand(id: "sandbox/foobar"), me)

		then:
		s.name == "sandbox/foobar"
	}

	void "createStream results in persisted Stream"() {
		when:
		service.createStream(new CreateStreamCommand(id: "sandbox/foobar"), me)

		then:
		Stream.count() == 1
		Stream.list().first().id == "sandbox/foobar"
	}

	void "createStream results in all permissions for Stream"() {
		when:
		def stream = service.createStream(new CreateStreamCommand(name: "name"), me)

		then:
		Permission.findAllByStream(stream)*.toMap() == [
			[id: 1, user: "me", operation: Permission.Operation.STREAM_GET.id],
			[id: 2, user: "me", operation: Permission.Operation.STREAM_EDIT.id],
			[id: 3, user: "me", operation: Permission.Operation.STREAM_DELETE.id],
			[id: 4, user: "me", operation: Permission.Operation.STREAM_PUBLISH.id],
			[id: 5, user: "me", operation: Permission.Operation.STREAM_SUBSCRIBE.id],
			[id: 6, user: "me", operation: Permission.Operation.STREAM_SHARE.id],
		]
	}

	void "createStream uses its params"() {
		when:
		def params = new CreateStreamCommand(
				name: "Test stream",
				description: "Test stream",
				requireSignedData: true
		)
		service.createStream(params, me)

		then: "stream is created"
		Stream.count() == 1
		def stream = Stream.findAll().get(0)
		stream.name == "Test stream"
		stream.description == "Test stream"
		stream.requireSignedData
	}

	void "getStreamEthereumPublishers should return Ethereum addresses of users with write permission to the Stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		User user1 = new User(id: 1, username: "u1").save(failOnError: true, validate: false)
		IntegrationKey key1 = new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		User user2 = new User(id: 2, username: "u2").save(failOnError: true, validate: false)
		IntegrationKey key2 = new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		User user3 = new User(id: 3, username: "u3").save(failOnError: true, validate: false)

		// User with key but no write permission - this key should not be returned by the query
		User userWithKeyButNoPermission = new User(id: 4, username: "u4").save(failOnError: true, validate: false)
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
		1 * service.permissionService.getPermissionsTo(stream, Permission.Operation.STREAM_PUBLISH) >> perms
		addresses == validAddresses
	}

	void "isStreamEthereumPublisher should return true iff user has write permission to the stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		User user1 = new User(id: 1, username: "u1").save(failOnError: true, validate: false)
		String address1 = "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57"
		new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: address1).save(failOnError: true, validate: false)
		User user2 = new User(id: 2, username: "u2").save(failOnError: true, validate: false)
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
		2 * service.permissionService.check(user1, stream, Permission.Operation.STREAM_PUBLISH) >> true
		result1 && result1b
		2 * service.permissionService.check(user2, stream, Permission.Operation.STREAM_PUBLISH) >> false
		!result2 && !result2b
	}

	void "getStreamEthereumSubscribers should return Ethereum addresses of users with stream_get permission to the Stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		User user1 = new User(id: 1, username: "u1").save(failOnError: true, validate: false)
		IntegrationKey key1 = new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		User user2 = new User(id: 2, username: "u2").save(failOnError: true, validate: false)
		IntegrationKey key2 = new IntegrationKey(user: user2, service: IntegrationKey.Service.ETHEREUM,
			idInService: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)
		User user3 = new User(id: 3, username: "u3").save(failOnError: true, validate: false)

		// User with key but no read permission - this key should not be returned by the query
		User userWithKeyButNoPermission = new User(id: 4, username: "u4").save(failOnError: true, validate: false)
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
		1 * service.permissionService.getPermissionsTo(stream, Permission.Operation.STREAM_SUBSCRIBE) >> perms
		addresses == validAddresses
	}

	void "isStreamEthereumSubscriber should return true iff user has stream_get permission to the stream"() {
		setup:
		service.permissionService = Mock(PermissionService)
		User user1 = new User(id: 1, username: "u1").save(failOnError: true, validate: false)
		String address1 = "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57"
		new IntegrationKey(user: user1, service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: address1).save(failOnError: true, validate: false)
		User user2 = new User(id: 2, username: "u2").save(failOnError: true, validate: false)
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
		2 * service.permissionService.check(user1, stream, Permission.Operation.STREAM_SUBSCRIBE) >> true
		result1 && result1b
		2 * service.permissionService.check(user2, stream, Permission.Operation.STREAM_SUBSCRIBE) >> false
		!result2 && !result2b
	}

	void "status ok and has recent messages"() {
		setup:
		service.cassandraService = Mock(CassandraService)
		Stream s = new Stream([name: "Stream 1", inactivityThresholdHours: 48])
		s.id = "s1"

		Date now = newDate(2019, 1, 15, 11, 12, 06)
		Date timestamp = newDate(2019, 1, 14, 11, 12, 06)
		long expected = timestamp.getTime()
		StreamMessage msg = TestUtils.buildMsg("s1", 0, timestamp)

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
		StreamMessage msg = TestUtils.buildMsg("s1", 0, timestamp)
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
		StreamMessage msg = TestUtils.buildMsg("s1", 0, timestamp)

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
