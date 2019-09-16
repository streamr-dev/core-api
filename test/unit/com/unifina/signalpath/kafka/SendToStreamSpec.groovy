package com.unifina.signalpath.kafka

import com.streamr.client.protocol.message_layer.MessageRef
import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV31
import com.unifina.BeanMockingSpecification
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.security.Userish
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakeStreamService
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.security.AccessControlException

@TestMixin(GrailsUnitTestMixin)
@Mock([SecUser, Stream])
class SendToStreamSpec extends BeanMockingSpecification {

	static class AllPermissionService extends PermissionService {
		@Override boolean canRead(Userish user, resource) { return true }
		@Override boolean canWrite(Userish user, resource) { return true }
		@Override boolean canShare(Userish user, resource) { return true }
	}

	static class ReadPermissionService extends PermissionService {
		@Override boolean canRead(Userish user, resource) { return true }
		@Override boolean canWrite(Userish user, resource) { return false }
		@Override boolean canShare(Userish user, resource) { return false }
	}

	static class WritePermissionService extends PermissionService {
		@Override boolean canRead(Userish user, resource) { return true }
		@Override boolean canWrite(Userish user, resource) { return true }
		@Override boolean canShare(Userish user, resource) { return false }
	}

	SecUser user
	FakeStreamService mockStreamService
	StreamService streamService
	Globals globals
	SendToStream module
	Stream stream

    def setup() {
		defineBeans {
			streamService(FakeStreamService)
			permissionService(AllPermissionService)
		}

		def user = new SecUser(name: "test user", username: 'test user')
		user.save(failOnError: true, validate: false)

		stream = new Stream()
		stream.id = stream.name = "stream-0"
		stream.config = [fields: [
			[name: "strIn", type: "string"],
			[name: "numIn", type: "number"],
		]]
		stream.save(validate: false, failOnError: true)

		Stream uiChannel = new Stream()
		uiChannel.id = uiChannel.name = "uiChannel"
		uiChannel.save(validate: false, failOnError: true)

		mockStreamService = (FakeStreamService) grailsApplication.getMainContext().getBean("streamService")
		globals = Spy(Globals, constructorArgs: [[:], user, Globals.Mode.REALTIME])
    }

	private void createModule(options = [:]) {
		module = new SendToStream()
		module.globals = globals
		module.parentSignalPath = new SignalPath(true)
		module.parentSignalPath.setGlobals(globals)
		module.parentSignalPath.configure([uiChannel: [id: "uiChannel"]])
		module.parentSignalPath.initialize()
		module.init()
		module.configure([
				params: [
						[name: "stream", value: "stream-0"]
				],
				options: options
		])
	}

	private void assertSequencing(List<StreamMessage> messages) {
		if (messages.size() == 0) {
			return
		}
		assert messages.get(0).sequenceNumber == 0L
		MessageRef previous = null
		messages.each {
			MessageRef ref = ((StreamMessageV31) it).previousMessageRef
			if (ref == null) {
				assert previous == null
			} else {
				assert ref.timestamp == previous.timestamp
				assert ref.sequenceNumber == previous.sequenceNumber
			}
			previous = new MessageRef(it.timestamp, it.sequenceNumber)
		}
	}

	void "SendToStream sends correct data to Kafka"() {
		createModule()

		Map inputValues = [
			strIn: ["a", "b", "c", "d"],
			numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [:]

		when:
		true

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				Map<String, List<Map>> sentContentByChannel = new HashMap<>()
				mockStreamService.sentMessagesByChannel.entrySet().each { entry ->
					sentContentByChannel.put(entry.key, entry.value.collect{it.getContent()})
				}
				assert sentContentByChannel == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0]
					]
				]
				assertSequencing(mockStreamService.sentMessagesByChannel.get("stream-0"))
				mockStreamService.sentMessagesByChannel = [:]
			}.test()
	}

	void "SendToStream throws exception if user does not have write access to stream"() {
		defineBeans {
			permissionService(ReadPermissionService)
		}

		when:
		createModule()
		then:
		2 * globals.isRunContext() >> true
		thrown(AccessControlException)
	}

	void "SendToStream does not throw AccessControlException if user has write permission to stream"() {
		defineBeans {
			permissionService(WritePermissionService)
		}

		when:
		createModule()
		then:
		2 * globals.isRunContext() >> true
		notThrown(AccessControlException)
	}

	void "SendToStream does not throw AccessControlException if not in run context"() {
		defineBeans {
			permissionService(ReadPermissionService)
		}

		when:
		createModule()
		then:
		2 * globals.isRunContext() >> false
		notThrown(AccessControlException)
	}

	void "Changing stream parameter during run time re-routes messages to new stream"() {
		createModule()

		def s2 = new Stream()
		s2.id = s2.name = "stream-1"
		s2.config = Stream.load("stream-0").config
		s2.save(validate: false, failOnError: true)

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
				Map<String, List<Map>> sentContentByChannel = new HashMap<>()
				mockStreamService.sentMessagesByChannel.entrySet().each { entry ->
					sentContentByChannel.put(entry.key, entry.value.collect{it.getContent()})
				}
				assert sentContentByChannel == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
					],
					"stream-1": [
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0],
					]
				]
				assertSequencing(mockStreamService.sentMessagesByChannel.get("stream-0"))
				assertSequencing(mockStreamService.sentMessagesByChannel.get("stream-1"))
				mockStreamService.sentMessagesByChannel = [:]
			}.test()
	}

	void "SendToStream by default sends old values along with new values"() {
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
			Map<String, List<Map>> sentContentByChannel = new HashMap<>()
			mockStreamService.sentMessagesByChannel.entrySet().each { entry ->
				sentContentByChannel.put(entry.key, entry.value.collect{it.getContent()})
			}
			assert sentContentByChannel == [
				"stream-0": [
					[strIn: "a", numIn: 1.0],
					[strIn: "a", numIn: 2.0],
					[strIn: "c", numIn: 2.0],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
			]
			assertSequencing(mockStreamService.sentMessagesByChannel.get("stream-0"))
			mockStreamService.sentMessagesByChannel = [:]
		}.test()
	}

	void "SendToStream can be configured to send only new values"() {
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
			Map<String, List<Map>> sentContentByChannel = new HashMap<>()
			mockStreamService.sentMessagesByChannel.entrySet().each { entry ->
				sentContentByChannel.put(entry.key, entry.value.collect{it.getContent()})
			}
			assert sentContentByChannel == [
				"stream-0": [
					[strIn: "a", numIn: 1.0],
					[numIn: 2.0],
					[strIn: "c"],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
			]
			assertSequencing(mockStreamService.sentMessagesByChannel.get("stream-0"))
			mockStreamService.sentMessagesByChannel = [:]
		}.test()
	}

}
