package com.unifina.signalpath.kafka

import com.unifina.data.FeedEvent
import com.unifina.datasource.HistoricalDataSource
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.kafka.KafkaHistoricalFeed
import com.unifina.feed.kafka.KafkaKeyProvider
import com.unifina.feed.map.MapMessageEventRecipient
import com.unifina.service.FeedService
import com.unifina.service.KafkaService
import com.unifina.service.PermissionService
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.utils.ConfigurableStreamModule
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakePushChannel
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import java.security.AccessControlException

@TestMixin(GrailsUnitTestMixin)
@Mock([SecUser, Stream, Feed])
class SendToStreamSpec extends Specification {

	static class AllPermissionService extends PermissionService {
		@Override boolean canRead(SecUser user, resource) { return true }
		@Override boolean canWrite(SecUser user, resource) { return true }
		@Override boolean canShare(SecUser user, resource) { return true }
	}

	static class ReadPermissionService extends PermissionService {
		@Override boolean canRead(SecUser user, resource) { return true }
		@Override boolean canWrite(SecUser user, resource) { return false }
		@Override boolean canShare(SecUser user, resource) { return false }
	}

	static class WritePermissionService extends PermissionService {
		@Override boolean canRead(SecUser user, resource) { return true }
		@Override boolean canWrite(SecUser user, resource) { return true }
		@Override boolean canShare(SecUser user, resource) { return false }
	}

	static class FakeKafkaService extends KafkaService {
		def receivedMessages = [:]

		@Override
		void sendMessage(Stream stream, Object key, Map message) {
			if (!receivedMessages.containsKey(stream.id)) {
				receivedMessages[stream.id] = []
			}
			receivedMessages[stream.id] << message
		}
	}

	SecUser user
	FakeKafkaService fakeKafkaService
	Globals globals
	SendToStream module
	Stream stream

    def setup() {
		defineBeans {
			kafkaService(FakeKafkaService)
			feedService(FeedService)
			permissionService(AllPermissionService)
		}

		def user = new SecUser(name: "test user")
		user.save(failOnError: true, validate: false)

		def feed = new Feed()
		feed.id = Feed.KAFKA_ID
		feed.backtestFeed = KafkaHistoricalFeed.getName()
		feed.eventRecipientClass = MapMessageEventRecipient.getName()
		feed.keyProviderClass = KafkaKeyProvider.getName()
		feed.timezone = "UTC"
		feed.save(validate: false, failOnError: true)

		stream = new Stream()
		stream.feed = feed
		stream.id = stream.name = "stream-0"
		stream.user = user
		stream.config = [fields: [
			[name: "strIn", type: "string"],
			[name: "numIn", type: "number"],
		]]
		stream.save(validate: false, failOnError: true)

		fakeKafkaService = (FakeKafkaService) grailsApplication.getMainContext().getBean("kafkaService")
		globals = Spy(Globals, constructorArgs: [[:], grailsApplication, user])
		globals.realtime = true
		globals.uiChannel = new FakePushChannel()
		globals.dataSource = new RealtimeDataSource()
    }

	private void createModule(options = [:]) {
		module = new SendToStream()
		module.globals = globals
		module.parentSignalPath = new SignalPath()
		module.parentSignalPath.setGlobals(globals)
		module.init()
		module.configure([
				params: [
						[name: "stream", value: "stream-0"]
				],
				options: options
		])
	}
	
	void "SendToStream sends correct data to Kafka"() {
		createModule()

		when:
		Map inputValues = [
			strIn: ["a", "b", "c", "d"],
			numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [:]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.afterEachTestCase {
				assert fakeKafkaService.receivedMessages == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0]
					]
				]
				fakeKafkaService.receivedMessages = [:]
			}.test()
	}

	void "SendToStream throws exception if user does not have write access to stream"() {
		defineBeans {
			permissionService(ReadPermissionService)
		}

		when:
		createModule()
		then:
		1 * globals.isRunContext() >> true
		thrown(AccessControlException)
	}

	void "SendToStream does not throw AccessControlException if user has write permission to stream"() {
		defineBeans {
			permissionService(WritePermissionService)
		}

		when:
		createModule()
		then:
		1 * globals.isRunContext() >> true
		notThrown(AccessControlException)
	}

	void "SendToStream does not throw AccessControlException if not in run context"() {
		defineBeans {
			permissionService(ReadPermissionService)
		}

		when:
		createModule()
		then:
		1 * globals.isRunContext() >> false
		notThrown(AccessControlException)
	}

	void "Changing stream parameter during run time re-routes messages to new stream"() {
		createModule()

		def s2 = new Stream()
		s2.feed = Feed.load(Feed.KAFKA_ID)
		s2.id = s2.name = "stream-1"
		s2.config = Stream.load("stream-0").config
		s2.user = user
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
				assert fakeKafkaService.receivedMessages == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
					],
					"stream-1": [
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0],
					]
				]
				fakeKafkaService.receivedMessages = [:]
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
			assert fakeKafkaService.receivedMessages == [
				"stream-0": [
					[strIn: "a", numIn: 1.0],
					[strIn: "a", numIn: 2.0],
					[strIn: "c", numIn: 2.0],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
			]
			fakeKafkaService.receivedMessages = [:]
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
			assert fakeKafkaService.receivedMessages == [
				"stream-0": [
					[strIn: "a", numIn: 1.0],
					[numIn: 2.0],
					[strIn: "c"],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
			]
			fakeKafkaService.receivedMessages = [:]
		}.test()
	}

	void "events should be produced to DataSource event queue in historical mode"() {
		globals.dataSource = new HistoricalDataSource(globals)
		globals.setRealtime(false)
		createModule()

		// Create the module that subscribes to our target stream
		ConfigurableStreamModule sourceModule = new ConfigurableStreamModule()
		sourceModule.init()
		sourceModule.getInput("stream").receive(stream)
		globals.dataSource.register(sourceModule)

		when:
		Map inputValues = [
				strIn: ["a", "b", "c", "d"],
				numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.overrideGlobals { globals }
				.beforeEachTestCase {
					globals.time = new Date()
				}.afterEachTestCase {
					// No messages have really been sent to Kafka
					assert fakeKafkaService.receivedMessages.isEmpty()

					// Correct events have been inserted to event queue
					for (int i=0; i<inputValues.strIn.size(); i++) {
						FeedEvent e = globals.getDataSource().getEventQueue().poll()
						assert e != null

						// Values are correct
						assert e.content.payload.strIn == inputValues.strIn[i]
						assert e.content.payload.numIn == inputValues.numIn[i]

						// Event recipient is correct
						assert e.recipient.modules.find {it == sourceModule}
					}

					// No other events have been inserted
					assert globals.getDataSource().getEventQueue().isEmpty()
				}.test()

	}
}
