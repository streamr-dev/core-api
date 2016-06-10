package com.unifina.signalpath.kafka

import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.FeedService
import com.unifina.service.KafkaService
import com.unifina.service.MetricsService
import com.unifina.service.PermissionService
import com.unifina.signalpath.ModuleSpecification
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals

import com.unifina.utils.testutils.FakePushChannel
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
@Mock([Stream, Feed])
class SendToStreamSpec extends Specification {

	static class FakePermissionService extends PermissionService {
		@Override boolean canRead(SecUser user, resource) { return true }
		@Override boolean canWrite(SecUser user, resource) { return true }
		@Override boolean canShare(SecUser user, resource) { return true }
	}

	static class FakeKafkaService extends KafkaService {
		def receivedMessages = []

		@Override
		void sendMessage(Stream stream, Object key, Map message) {
			receivedMessages << message
		}
	}

	FakeKafkaService fakeKafkaService
	Globals globals
	SendToStream module

    def setup() {
		defineBeans {
			kafkaService(FakeKafkaService)
			feedService(FeedService)
			permissionService(FakePermissionService)
			metricsService(ModuleSpecification.MockMetricsService)
		}

		def feed = new Feed()
		feed.id = 7
		feed.save(false)

		def s = new Stream()
		s.feed = feed
		s.id = s.name = "stream-0"
		s.config = [fields: [
			[name: "strIn", type: "string"],
			[name: "numIn", type: "number"],
		]]
		s.save(false)

		module = new SendToStream()
		module.globals = globals = new Globals([:], grailsApplication, null)
		module.parentSignalPath = new SignalPath()
		module.init()
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
		])

		fakeKafkaService = (FakeKafkaService) grailsApplication.getMainContext().getBean("kafkaService")
    }
	
	void "sendToStream sends correct data to Kafka"() {
		when:
		Map inputValues = [
			strIn: ["a", "b", "c", "d"],
			numIn: [1, 2, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [:]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals {
				globals.realtime = true
				globals.uiChannel = new FakePushChannel()
				globals.dataSource = new RealtimeDataSource()
				globals
			}
			.afterEachTestCase {
				assert fakeKafkaService.receivedMessages == [
					[strIn:"a", numIn:1.0],
					[strIn:"b", numIn:2.0],
					[strIn:"c", numIn:3.0],
					[strIn:"d", numIn:4.0]
				]
				fakeKafkaService.receivedMessages = []
			}.test()
	}
}
