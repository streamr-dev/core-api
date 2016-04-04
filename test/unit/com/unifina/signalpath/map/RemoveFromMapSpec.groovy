package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class RemoveFromMapSpec extends Specification {
	RemoveFromMap module

	def setup() {
		module = new RemoveFromMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "RemoveFromMap works as expected"() {

		Map inputValues = [
		    in: [
		        [:],
				[a: "a", b: 6d],
				[a: "a", b: 6d, c: 32d],
				Collections.unmodifiableMap([d: "d", e: "e"])
		    ],
			key: ["a", "a", "b", "d"],
		]

		Map outputValues = [
			out: [
				[:],
				[b: 6d],
				[a: "a", c: 32d],
				[e: "e"]
			],
			item: [null, "a", 6d, "d"]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
