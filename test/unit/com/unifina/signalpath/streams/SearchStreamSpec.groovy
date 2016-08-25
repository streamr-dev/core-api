package com.unifina.signalpath.streams

import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(Stream)
class SearchStreamSpec extends Specification {

	SearchStream module
	Globals globals

	def setup() {
		module = new SearchStream()
		module.init()

		globals = Mock(Globals)
		globals.getBean(StreamService.class) >> new StreamService()

		Stream stream = new Stream(name: "exists")
		stream.id = "666-666-666-999"
		stream.save(failOnError: true, validate: false)

		Stream stream2 = new Stream(name: "exists-with-fields")
		stream2.id = "111-333-111"
		stream2.config = "{'fields': [{'name': 'a', 'type': 'number'}, {'name': 'b', 'type': 'string'}]}"
		stream2.save(failOnError: true, validate: false)
	}

	void "searchStream works as expected"() {
		when:
		Map inputValues = [
			name: ["doesnotexist", "exists", "doesnotexist2", "exists-with-fields"]
		]
		Map outputValues = [
			"stream": [null, "666-666-666-999", "666-666-666-999", "111-333-111"],
			"found": [false, true, false, true],
			"fields": [null, null, null, [a: "number", b: "string"]]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}
}
