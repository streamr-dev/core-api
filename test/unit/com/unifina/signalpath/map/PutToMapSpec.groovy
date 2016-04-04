package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PutToMapSpec extends Specification {
	PutToMap module

	def setup() {
		module = new PutToMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "PutToMap() works as expected"() {

		Map inputValues = [
		    map: [
		        [:],
				[a: "a", b: 6d],
				[a: "a", b: 6d, c: 32d],
				Collections.unmodifiableMap([d: "d"])
		    ],
			key: ["a", "c", "c", "a"],
			value: ["hello", "world", "!!!", "."]
		]

		Map outputValues = [
			map: [
			    [a: "hello"],
				[a: "a", b: 6d, c: "world"],
				[a: "a", b: 6d, c: "!!!"],
				[a: ".", d: "d"]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
