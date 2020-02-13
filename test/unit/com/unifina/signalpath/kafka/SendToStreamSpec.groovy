package com.unifina.signalpath.kafka

import com.streamr.client.options.StreamrClientOptions
import com.unifina.BeanMockingSpecification
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.service.StreamrClientService
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakeStreamrClient
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.security.AccessControlException

@TestMixin(GrailsUnitTestMixin)
@Mock([SecUser, Stream])
class SendToStreamSpec extends BeanMockingSpecification {

	SecUser user
	Globals globals
	SendToStream module
	Stream stream
	FakeStreamrClient streamrClient
	PermissionService permissionService
	StreamService streamService

    def setup() {
		def user = new SecUser(name: "test user", username: 'test user')
		user.save(failOnError: true, validate: false)

		StreamrClientService streamrClientService = mockBean(StreamrClientService, Mock(StreamrClientService))
		permissionService = mockBean(PermissionService, Mock(PermissionService))
		streamService = mockBean(StreamService, Mock(StreamService))

		stream = new Stream()
		stream.id = stream.name = "stream-0"
		stream.config = [fields: [
			[name: "strIn", type: "string"],
			[name: "numIn", type: "number"],
		]]
		stream.save(validate: false) // deserialization won't work without saving

		streamrClient = new FakeStreamrClient(new StreamrClientOptions())
		streamrClientService.getAuthenticatedInstance(_) >> streamrClient

		streamService.getStream(stream.id) >> stream

		globals = Spy(Globals, constructorArgs: [[:], user, Globals.Mode.REALTIME])
    }

	private void createModule(options = [:]) {
		module = new SendToStream()
		module.globals = globals
		module.init()
		module.configure([
				params: [
						[name: "stream", value: "stream-0"]
				],
				options: options
		])
	}

	void "SendToStream sends correct data to StreamrClient"() {
		permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		createModule()

		Map inputValues = [
			strIn: ["a", "b", "c", "d"],
			numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [:]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				def sentMessagesByStream = streamrClient.getAndClearSentMessages()
				assert sentMessagesByStream.keySet().asList() == [stream.id]
				assert sentMessagesByStream[stream.id]*.payload == [
					[strIn:"a", numIn:1.0],
					[strIn:"b", numIn:2.0],
					[strIn:"c", numIn:3.0],
					[strIn:"d", numIn:4.0]
				]
			}.test()
	}

	void "SendToStream throws exception on creation if user does not have write access to stream"() {
		when:
		createModule()

		then:
		1 * globals.isRunContext() >> true
		1 * permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> false
		thrown(AccessControlException)
	}

	void "SendToStream does not throw AccessControlException if user has write permission to stream"() {
		when:
		createModule()

		then:
		1 * globals.isRunContext() >> true
		1 * permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		notThrown(AccessControlException)
	}

	void "SendToStream does not check permissions if not in run context"() {
		when:
		createModule()

		then:
		1 * globals.isRunContext() >> false
		0 * permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH)
		notThrown(AccessControlException)
	}

	void "Changing stream parameter during run time re-routes messages to new stream"() {
		permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		createModule()

		def s2 = new Stream()
		s2.id = s2.name = "stream-1"
		s2.config = stream.config
		s2.save(validate: false) // deserialization won't work without saving

		streamService.getStream(s2.id) >> s2

		when:
		Map inputValues = [
			stream: ["stream-0", null, "stream-1", null],
			strIn: ["a", "b", "c", "d"],
			numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				def sentMessagesByStream = streamrClient.getAndClearSentMessages()
				assert sentMessagesByStream.keySet() == [stream.id, s2.id].toSet()
				assert sentMessagesByStream[stream.id]*.payload == [
					[strIn:"a", numIn:1.0],
					[strIn:"b", numIn:2.0],
				]
				assert sentMessagesByStream[s2.id]*.payload == [
					[strIn:"c", numIn:3.0],
					[strIn:"d", numIn:4.0],
				]
			}.test()
	}

	void "SendToStream by default sends old values along with new values"() {
		permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		createModule()

		when:
		Map inputValues = [
			strIn: ["a", null, "c", "d", null, "f"],
			numIn: [  1,    2, null,  4, null,   6].collect {it?.doubleValue()},
		]
		Map outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				def sentMessagesByStream = streamrClient.getAndClearSentMessages()
				assert sentMessagesByStream.keySet().asList() == [stream.id]
				assert sentMessagesByStream[stream.id]*.payload == [
					[strIn: "a", numIn: 1.0],
					[strIn: "a", numIn: 2.0],
					[strIn: "c", numIn: 2.0],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
		}.test()
	}

	void "SendToStream can be configured to send only new values"() {
		permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		createModule([sendOnlyNewValues: [value: true]])

		when:
		Map inputValues = [
			strIn: ["a", null, "c", "d", null, "f"],
			numIn: [  1,    2, null,  4, null,   6].collect {it?.doubleValue()},
		]
		Map outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				def sentMessagesByStream = streamrClient.getAndClearSentMessages()
				assert sentMessagesByStream.keySet().asList() == [stream.id]
				assert sentMessagesByStream[stream.id]*.payload == [
					[strIn: "a", numIn: 1.0],
					[numIn: 2.0],
					[strIn: "c"],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
		}.test()
	}

	void "SendToStream exposes a partitionKey input when the stream has multiple partitions"() {
		stream.partitions = 10
		stream.save(validate: false)

		permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		createModule()

		expect:
		module.getInput("partitionKey") != null
	}

	void "SendToStream passes the partitionKey to StreamrClient when available"() {
		stream.partitions = 10
		stream.save(validate: false)

		permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> true
		createModule()

		Map inputValues = [
			strIn: ["a", "b", "c", "d"],
			numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
			partitionKey: ["x", "y", "z", null]
		]
		Map outputValues = [:]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				def sentMessagesByStream = streamrClient.getAndClearSentMessages()
				assert sentMessagesByStream.keySet().asList() == [stream.id]
				assert sentMessagesByStream[stream.id]*.payload == [
					[strIn:"a", numIn:1.0],
					[strIn:"b", numIn:2.0],
					[strIn:"c", numIn:3.0],
					[strIn:"d", numIn:4.0]
				]
				// last non-null value "z" lingers in the input if sendOnlyNewValues is false
				assert sentMessagesByStream[stream.id]*.partitionKey == ["x", "y", "z", "z"]
			}.test()
	}

}
