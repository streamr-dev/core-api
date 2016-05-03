package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class GetFromMapSpec extends ModuleSpecification {
	GetFromMap module

	def setup() {
		module = new GetFromMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "getFromMap() works as expected"() {

		Map inputValues = [
			key: ["a", "b", "a", "d"],
		    in: [
		        [:],
				[a: 0d, b: 1d],
				[a: 6.66d, b: "b"],
				[a: 3.5d, b: new Object(), c: "sss", d: "ddd"]
		    ]
		]

		Map outputValues = [
			found: [ 0, 1, 1, 1 ]*.doubleValue(),
			out: [ null, 1d, 6.66d, "ddd" ],
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
