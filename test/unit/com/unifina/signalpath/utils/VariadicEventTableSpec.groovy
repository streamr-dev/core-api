package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VariadicEventTableSpec extends Specification {

	def final static format = "yyyy-MM-dd HH:mm:ss.SSS";

	VariadicEventTable module

	def setup() {
		module = new VariadicEventTable()
		module.init()
		module.getInput("in1")
		module.getInput("in2")
		module.getInput("in3")
		module.getInput("in4")
		module.configure([uiChannel: [id: "table"]])
	}

	void "eventTable sends correct data to uiChannel"() {
		when:
		Map inputValues = [
			in1: ["a", "b", "c", "d"],
			in2: [1, 2, 3, 4],
			in3: [null, null, "hello", "world"],
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["timestamp", "outputForin1", "outputForin2", "outputForin3"], title: null]],
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