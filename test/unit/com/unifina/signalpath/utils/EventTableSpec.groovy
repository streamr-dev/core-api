package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

import java.text.SimpleDateFormat

class EventTableSpec extends Specification {

	def final static format = "yyyy-MM-dd HH:mm:ss.SSS";

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
				[hdr: [headers: ["timestamp", "outputForinput1", "outputForinput2", "outputForinput3"]]],
				[nr: [new Date(0).format(format), "a", "1", null]],
				[nr: [new Date(60 * 1000).format(format), "b", "2", null]],
				[nr: [new Date(60 * 1000 * 2).format(format), "c", "3", "hello"]],
				[nr: [new Date(60 * 1000 * 3).format(format), "d", "4", "world"]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.timeToFurtherPerIteration(60 * 1000)
			.test()
	}
}