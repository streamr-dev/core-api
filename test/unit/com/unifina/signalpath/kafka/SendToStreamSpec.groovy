package com.unifina.signalpath.kafka

import com.unifina.BeanMockingSpecification
import com.unifina.data.FeedEvent
import com.unifina.datasource.HistoricalDataSource
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.NoOpStreamListener
import com.unifina.feed.StreamrBinaryMessageKeyProvider
import com.unifina.feed.cassandra.CassandraHistoricalFeed
import com.unifina.feed.map.MapMessageEventRecipient
import com.unifina.security.Userish
import com.unifina.service.FeedService
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.utils.ConfigurableStreamModule
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakeStreamService
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.security.AccessControlException

@TestMixin(GrailsUnitTestMixin)
@Mock([SecUser, Stream, Feed])
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
			feedService(FeedService)
			permissionService(AllPermissionService)
		}

		def user = new SecUser(name: "test user")
		user.save(failOnError: true, validate: false)

		def feed = new Feed()
		feed.id = Feed.KAFKA_ID
		feed.backtestFeed = CassandraHistoricalFeed.getName()
		feed.eventRecipientClass = MapMessageEventRecipient.getName()
		feed.keyProviderClass = StreamrBinaryMessageKeyProvider.getName()
		feed.streamListenerClass = NoOpStreamListener.getName()
		feed.timezone = "UTC"
		feed.save(validate: false, failOnError: true)

		stream = new Stream()
		stream.feed = feed
		stream.id = stream.name = "stream-0"
		stream.config = [fields: [
			[name: "strIn", type: "string"],
			[name: "numIn", type: "number"],
		]]
		stream.save(validate: false, failOnError: true)

		Stream uiChannel = new Stream()
		uiChannel.feed = feed
		uiChannel.id = uiChannel.name = "uiChannel"
		uiChannel.save(validate: false, failOnError: true)

		mockStreamService = (FakeStreamService) grailsApplication.getMainContext().getBean("streamService")
		globals = Spy(Globals, constructorArgs: [[:], user])
		globals.realtime = true
		globals.dataSource = new RealtimeDataSource()
    }

	private void createModule(options = [:]) {
		module = new SendToStream()
		module.globals = globals
		module.parentSignalPath = new SignalPath(true)
		module.parentSignalPath.setGlobals(globals)
		module.parentSignalPath.configure([uiChannel: [id: "uiChannel"]])
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
				assert mockStreamService.sentMessagesByChannel == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0]
					]
				]
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
				assert mockStreamService.sentMessagesByChannel == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
					],
					"stream-1": [
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0],
					]
				]
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
			assert mockStreamService.sentMessagesByChannel == [
				"stream-0": [
					[strIn: "a", numIn: 1.0],
					[strIn: "a", numIn: 2.0],
					[strIn: "c", numIn: 2.0],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
			]
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
			assert mockStreamService.sentMessagesByChannel == [
				"stream-0": [
					[strIn: "a", numIn: 1.0],
					[numIn: 2.0],
					[strIn: "c"],
					[strIn: "d", numIn: 4.0],
					[strIn: "f", numIn: 6.0]
				]
			]
			mockStreamService.sentMessagesByChannel = [:]
		}.test()
	}

	void "events should be produced to DataSource event queue in historical mode"() {
		List<FeedEvent> enqueuedEvents = []
		globals.dataSource = new HistoricalDataSource(globals) {
			@Override
			void enqueueEvent(FeedEvent feedEvent) {
				super.enqueueEvent(feedEvent)
				enqueuedEvents.push(feedEvent)
			}
		}
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
					// No messages have really been sent to the stream
					assert mockStreamService.sentMessagesByChannel[stream.id] == null
					// One notification has been sent to the parentSignalPath ui channel
					assert mockStreamService.sentMessagesByChannel[module.parentSignalPath.uiChannel.id].size() == 1

					// Correct events have been inserted to event queue
					for (int i=0; i<inputValues.strIn.size(); i++) {
						FeedEvent e = enqueuedEvents.remove(0)
						assert e != null

						// Values are correct
						assert e.content.payload.strIn == inputValues.strIn[i]
						assert e.content.payload.numIn == inputValues.numIn[i]

						// Event recipient is correct
						assert e.recipient.modules.find {it == sourceModule}
					}

					// No other events have been inserted
					assert enqueuedEvents.isEmpty()

					mockStreamService.sentMessagesByChannel = [:]
				}.test()

	}
}
