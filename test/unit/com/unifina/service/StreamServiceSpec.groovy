package com.unifina.service


import com.unifina.domain.ExampleType
import com.unifina.domain.Permission
import com.unifina.domain.Stream
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StreamService)
@Mock([Stream, User, Permission, PermissionService])
class StreamServiceSpec extends Specification {
	User me = new User(username: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9")

	def setup() {
		me.save(validate: false, failOnError: true)
	}

	def "add example shared streams"() {
		setup:
		service.permissionService = Mock(PermissionService)
		List<Stream> streams = []
		Stream s0 = new Stream(
			name: "example stream",
			exampleType: ExampleType.SHARE
		)
		s0.id = me.username + "/stream-0"
		s0.save(failOnError: true)
		streams << s0
		Stream s1 = new Stream(
			name: "example 2 stream",
			exampleType: ExampleType.SHARE
		)
		s1.id = me.username + "/stream-1"
		s1.save(failOnError: true)
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
		Stream s = service.createStream(new CreateStreamCommand(id: "foobar"), me, null)

		then:
		s.name == "foobar"
	}

	void "createStream results in persisted Stream"() {
		when:
		service.createStream(new CreateStreamCommand(id: "foobar"), me, null)

		then:
		Stream.count() == 1
		Stream.list().first().id == "foobar"
	}

	void "createStream results in all permissions for Stream"() {
		when:
		def stream = service.createStream(new CreateStreamCommand(id: me.username + "/name", name: "name"), me, null)

		then:
		Permission.findAllByStream(stream)*.toMap() == [
			[id: 1, user: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9", operation: Permission.Operation.STREAM_GET.id],
			[id: 2, user: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9", operation: Permission.Operation.STREAM_EDIT.id],
			[id: 3, user: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9", operation: Permission.Operation.STREAM_DELETE.id],
			[id: 4, user: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9", operation: Permission.Operation.STREAM_PUBLISH.id],
			[id: 5, user: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9", operation: Permission.Operation.STREAM_SUBSCRIBE.id],
			[id: 6, user: "0x83b20d83cd0565c6592b11b9bc5b5a0b2b8d6ba9", operation: Permission.Operation.STREAM_SHARE.id],
		]
	}

	void "createStream uses its params"() {
		when:
		def params = new CreateStreamCommand(
			id: me.username + "/test",
			name: "Test stream",
			description: "Test stream",
			requireSignedData: true
		)
		service.createStream(params, me, null)

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
		User user1 = new User(id: 1, username: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		User user2 = new User(id: 2, username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)

		// User with key but no write permission - this key should not be returned by the query
		User userWithKeyButNoPermission = new User(id: 4, username: "0x12345e3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)

		Set<String> validAddresses = new HashSet<String>()
		validAddresses.add(user1.getUsername())
		validAddresses.add(user2.getUsername())
		Permission p1 = new Permission(user: user1)
		Permission p2 = new Permission(user: user2)
		List<Permission> perms = [p1, p2]
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
		String address1 = "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57"
		User user1 = new User(id: 1, username: address1).save(failOnError: true, validate: false)
		String address2 = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		User user2 = new User(id: 2, username: address2).save(failOnError: true, validate: false)
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
		User user1 = new User(id: 1, username: "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57").save(failOnError: true, validate: false)
		User user2 = new User(id: 2, username: "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)

		// User with key but no read permission - this key should not be returned by the query
		User userWithKeyButNoPermission = new User(id: 4, username: "0x12345e3f5efe8a01eca8c2e9d3c32702cf4bead6").save(failOnError: true, validate: false)

		Set<String> validAddresses = new HashSet<String>()
		validAddresses.add(user1.getUsername())
		validAddresses.add(user2.getUsername())
		Permission p1 = new Permission(user: user1)
		Permission p2 = new Permission(user: user2)
		List<Permission> perms = [p1, p2]
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
		String address1 = "0x9fe1ae3f5efe2a01eca8c2e9d3c11102cf4bea57"
		User user1 = new User(id: 1, username: address1).save(failOnError: true, validate: false)
		String address2 = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		User user2 = new User(id: 2, username: address2).save(failOnError: true, validate: false)
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
}
