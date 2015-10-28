package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class EventTableSpec extends Specification {

	EventTable module

	def setup() {
		module = new EventTable()
		module.init()
		module.configure([
			uiChannel: [id: "table"],
			options: [inputs: [value: 3]]
		])
	}

	void "eventTable sends correct data to uiChannel"() {
		when:
		Map inputValues = [
			input1: ["a", "b", "c", "d"],
			input2: [1, 2, 3, 4],
			input3: [null, null, "hello", "world"],
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["timestamp", "input1", "input2", "input3"]]],
				[nr: ["1970-01-01 02:00:00.000", "a", "1", null]],
				[nr: ["1970-01-01 02:01:00.000", "b", "2", null]],
				[nr: ["1970-01-01 02:02:00.000", "c", "3", "hello"]],
				[nr: ["1970-01-01 02:03:00.000", "d", "4", "world"]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.timeToFurtherPerIteration(60 * 1000)
			.test()
	}
}