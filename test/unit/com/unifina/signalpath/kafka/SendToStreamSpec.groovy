package com.unifina.signalpath.kafka

import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.FeedService
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakePushChannel
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

import java.security.AccessControlException

@TestMixin(ControllerUnitTestMixin) // to get JSON converter
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

	static class MockStreamService extends StreamService {
		def receivedMessages = [:]

		@Override
		void sendMessage(Stream stream, Map message, int ttl=0) {
			if (!receivedMessages.containsKey(stream.id)) {
				receivedMessages[stream.id] = []
			}
			receivedMessages[stream.id] << message
		}
		
	}

	SecUser user
	MockStreamService mockStreamService
	StreamService streamService
	Globals globals
	SendToStream module

    def setup() {
		defineBeans {
			streamService(MockStreamService)
			feedService(FeedService)
			permissionService(AllPermissionService)
		}

		def user = new SecUser(name: "test user")
		user.save(failOnError: true, validate: false)

		def feed = new Feed()
		feed.id = Feed.KAFKA_ID
		feed.save(validate: false, failOnError: true)

		def s = new Stream()
		s.feed = feed
		s.id = s.name = "stream-0"
		s.user = user
		s.config = [fields: [
			[name: "strIn", type: "string"],
			[name: "numIn", type: "number"],
		]]
		s.save(validate: false, failOnError: true)

		mockStreamService = (MockStreamService) grailsApplication.getMainContext().getBean("streamService")
		globals = Spy(Globals, constructorArgs: [[:], grailsApplication, user])
		globals.realtime = true
		globals.uiChannel = new FakePushChannel()
		globals.dataSource = new RealtimeDataSource()
    }

	private void createModule() {
		module = new SendToStream()
		module.globals = globals
		module.parentSignalPath = new SignalPath()
		module.init()
		module.configure([
				params: [
						[name: "stream", value: "stream-0"]
				],
		])
	}
	
	void "sendToStream sends correct data to Kafka"() {
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
				assert mockStreamService.receivedMessages == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0]
					]
				]
				mockStreamService.receivedMessages = [:]
			}.test()
	}

	void "sendToStream throws exception if user does not have write access to stream"() {
		defineBeans {
			permissionService(ReadPermissionService)
		}

		when:
		createModule()
		then:
		1 * globals.isRunContext() >> true
		thrown(AccessControlException)
	}

	void "sendToStream does not throw AccessControlException if user has write permission to stream"() {
		defineBeans {
			permissionService(WritePermissionService)
		}

		when:
		createModule()
		then:
		1 * globals.isRunContext() >> true
		notThrown(AccessControlException)
	}

	void "sendToStream does not throw AccessControlException if not in run context"() {
		defineBeans {
			permissionService(ReadPermissionService)
		}

		when:
		createModule()
		then:
		1 * globals.isRunContext() >> false
		notThrown(AccessControlException)
	}

	void "changing stream parameter during run time re-routes messages to new stream"() {
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
				assert mockStreamService.receivedMessages == [
					"stream-0": [
						[strIn:"a", numIn:1.0],
						[strIn:"b", numIn:2.0],
					],
					"stream-1": [
						[strIn:"c", numIn:3.0],
						[strIn:"d", numIn:4.0],
					]
				]
				mockStreamService.receivedMessages = [:]
			}.test()
	}
}
