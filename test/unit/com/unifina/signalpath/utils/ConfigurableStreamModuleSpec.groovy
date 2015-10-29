package com.unifina.signalpath.utils

import com.unifina.domain.data.Stream
import com.unifina.service.FeedService
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
@TestFor(Stream)
class ConfigurableStreamModuleSpec extends Specification {

	static class FakeFeedService extends FeedService {
		@Override
		Stream getStream(String name) {
			def s = new Stream()
			s.name = name
			s.streamConfig = [fields: [[name: "out", type: "string"]]]
			s
		}
	}

	Globals globals
	ConfigurableStreamModule module

	def setup() {
		defineBeans {
			feedService(FakeFeedService){ bean -> bean.autowire = true }
		}

		module = new ConfigurableStreamModule()
		module.init()
		module.name = "streamModule"
		module.globals = globals = new Globals([:], grailsApplication, null)
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
		])
	}

	void "configurableStreamModule gives the right answer"() {

		when:
		Map inputValues = [
			stream: ["stream-1", "stream-2", "stream-3", "stream-4"],
		]
		Map outputValues = [
			out: [null, null, null, null] // TODO: is this module supposed to do something?
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}
}