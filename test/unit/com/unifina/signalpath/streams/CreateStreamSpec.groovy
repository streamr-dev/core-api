package com.unifina.signalpath.streams

import com.unifina.BeanMockingSpecification
import com.unifina.domain.Stream
import com.unifina.service.CreateStreamCommand
import com.unifina.service.StreamService
import com.unifina.service.ValidationException
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

@TestMixin(ControllerUnitTestMixin) // to get JSON converter
@Mock([Stream])
class CreateStreamSpec extends BeanMockingSpecification {

	CreateStream module
	Globals globals = Stub(Globals)
	StreamService streamService

	def setup() {
		module = new CreateStream()
		module.init()

		streamService = mockBean(StreamService, Mock(StreamService))
		globals.getUserId() >> null

		Stream stream = new Stream(name: "exists")
		stream.id = "666"
		stream.save(failOnError: true, validate: false)
	}

	void "creates streams from valid input value specifications"() {
		when:
		Map inputValues = [
			name: ["stream-1", "stream-2", "error"],
			description: ["my 1st stream", "", "error"],
			fields: [[:], [a: "boolean", b: "string"], [:]]
		]
		Map outputValues = [
			"stream": ["666", "111", "111"],
			"created": [true, true, false]
		]

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()

		then:
		4 * streamService.createStream(new CreateStreamCommand(
			name: "stream-1",
			description: "my 1st stream",
			config: [fields: []] as JSON,
		), null) >> {
			Stream s = new Stream()
			s.id = "666"
			return s
		}
		4 * streamService.createStream(new CreateStreamCommand(
			name: "stream-2",
			description: "",
			config: [fields: [[name: "a", type: "boolean"], [name: "b", type: "string"]]] as JSON,
		), null) >> {
			Stream s = new Stream()
			s.id = "111"
			return s
		}
		4 * streamService.createStream(new CreateStreamCommand(
			name: "error",
			description: "error",
			config: [fields: []] as JSON
		), null) >> {
			throw new ValidationException()
		}
		0 * streamService._
	}
}
