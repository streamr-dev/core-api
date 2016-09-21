package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ContainsValueSpec extends Specification {
	ContainsValue module

	def setup() {
		module = new ContainsValue()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "containsValue() works as expected"() {
		when:
		Map inputValues = [
			value: ["value", "non-existent-value", 666, 3.141592d],
		    in: [
		        [k1: "v1", k2: "v2", k3: "value"],
				[k1: "vvv", k2: "www", k3: "zzz"],
				[k1: "hallo", k2: 666],
				[k1: "hallo", k2: 666, k3: 3.141592d],
		    ],
		]

		Map outputValues = [
		    found: [true, false, true, true]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
