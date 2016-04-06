package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class KeysToListSpec extends Specification {
	KeysToList module

	def setup() {
		module = new KeysToList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "keysToList() works as expected"() {

		def lastMap = [a: 3.5d, b: new Object(), c: "sss", d: "ddd"]
		lastMap.put(100, "666")

		Map inputValues = [
		    in: [
		        [:],
				[a: 0d, b: 1d],
				[a: 6.66d, b: "b", c: "555"],
				lastMap
		    ]
		]

		Map outputValues = [
			keys: [[], ["a", "b"], ["a", "b", "c"], ["a", "b", "c", "d", 100]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
