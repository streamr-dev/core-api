package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ValuesToListSpec extends Specification {
	ValuesToList module

	def setup() {
		module = new ValuesToList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "valuesToList() works as expected"() {
		Map inputValues = [
		    in: [
		        [:],
				[a: 0d, b: 1d],
				[a: 6.66d, b: "b", c: "555"],
				[a: 3.5d, b: "b", c: "sss", d: "ddd", 100: "666"]
		    ]
		]

		Map outputValues = [
			keys: [[], [0d, 1d], [6.66d, "b", "555"], [3.5d, "b", "sss", "ddd", "666"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
