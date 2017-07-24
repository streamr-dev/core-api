package com.unifina.signalpath.streams

import com.unifina.ModuleTestingSpecification
import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import com.unifina.utils.testutils.ModuleTestHelper

class GetOrCreateStreamSpec extends ModuleTestingSpecification {

	GetOrCreateStream module
	StreamService streamService
	Stream stream
	Stream stream2

	def setup() {
		stream = new Stream(name: "exists")
		stream.id = "666-666-666-999"

		stream2 = new Stream(name: "exists-with-fields")
		stream2.id = "111-333-111"
		stream2.config = "{'fields': [{'name': 'x', 'type': 'number'}, {'name': 'y', 'type': 'string'}]}"

		streamService = Mock(StreamService)

		streamService.createStream(_, _) >> {params, user ->
			Stream s = new Stream()
			s.id = params.name
			return s
		}
		streamService.findByName("exists") >> stream
		streamService.findByName("exists-with-fields") >> stream2

		mockBean(StreamService, streamService)

		module = setupModule(new GetOrCreateStream())
	}

	def cleanup() {
		cleanupMockBeans()
	}

	void "GetOrCreateStreamSpec works as expected"() {
		Map inputValues = [
			name: ["doesnotexist", "exists", "doesnotexist2", "exists-with-fields", "doesnotexist"],
			description: ["test-stream"] * 5,
			fields: [[a: "boolean", b: "string"]] * 5
		]
		Map outputValues = [
			"stream": ["doesnotexist", "666-666-666-999", "doesnotexist2", "111-333-111", "doesnotexist"],
			"created": [true, false, true, false, false]
//			"fields": [fields, [:], fields, [x: "number", y: "string"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "found streams are cached"() {
		Map inputValues = [
				name: ["exists", "exists"],
				description: ["test-stream"] * 2,
				fields: [[a: "boolean", b: "string"]] * 2
		]
		Map outputValues = [
				"stream": ["666-666-666-999", "666-666-666-999"],
				"created": [false, false]
		]

		when:
		boolean pass = new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.serializationModes([ModuleTestHelper.SerializationMode.NONE].toSet())
				.test()

		then:
		pass
		1 * streamService.findByName("exists") >> stream
		0 * streamService.createStream(_, _)
	}
}
