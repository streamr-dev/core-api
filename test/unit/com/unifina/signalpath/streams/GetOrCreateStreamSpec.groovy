package com.unifina.signalpath.streams

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.StreamService
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Stream, Feed])
class GetOrCreateStreamSpec extends Specification {

	GetOrCreateStream module

	Globals globals = Stub(Globals)

	def setup() {
		module = new GetOrCreateStream()
		module.init()

		globals.getBean(StreamService.class) >> new StreamService() {
			@Override
			Stream createStream(Map params, SecUser user) {
				Stream s = new Stream()
				s.id = params.name
				return s
			}
		}
		globals.getUser() >> null

		Feed feed = new Feed(streamListenerClass: NoOpStreamListener.canonicalName)
		feed.id = 7
		feed.save(failOnError: true, validate: false)

		Stream stream = new Stream(name: "exists")
		stream.id = "666-666-666-999"
		stream.save(failOnError: true, validate: false)

		Stream stream2 = new Stream(name: "exists-with-fields")
		stream2.id = "111-333-111"
		stream2.config = "{'fields': [{'name': 'x', 'type': 'number'}, {'name': 'y', 'type': 'string'}]}"
		stream2.save(failOnError: true, validate: false)
	}

	void "GetOrCreateStreamSpec works as expected"() {
		when:
		Map inputValues = [
			name: ["doesnotexist", "exists", "doesnotexist2", "exists-with-fields"],
			description: ["test-stream"] * 4,
			fields: [[a: "boolean", b: "string"]] * 4
		]
		Map outputValues = [
			"stream": ["doesnotexist", "666-666-666-999", "doesnotexist2", "111-333-111"],
			"created": [true, false, true, false]
//			"fields": [fields, [:], fields, [x: "number", y: "string"]]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}
}
