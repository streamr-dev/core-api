package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class RepeatItemSpec extends Specification {
	RepeatItem module

	def setup() {
		module = new RepeatItem()
		module.init()
		module.configure(module.configuration)
	}

	def "RepeatItem works as expected"() {
		Map inputValues = [
			times: [5, null, 2, 1, 0],
			item: [10d, "abba", "o", "one", "zero"]
		]

		Map outputValues = [
			list: [
				[10d, 10d, 10d, 10d, 10d],
				["abba", "abba", "abba", "abba", "abba"],
				["o", "o"],
				["one"],
				[]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
