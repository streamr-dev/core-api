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
			key: ["a", "b", "a", "d", "pals[1].name", "derp.length", "asdf[10]"],
		    in: [
		        [:],
				[a: 0d, b: 1d],
				[a: 6.66d, b: "b"],
				[a: 3.5d, b: new Object(), c: "sss", d: "ddd"],
				[pals: [[name: "Jake", species: "Dog"], [name: "Finn", species: "Human"]], type: "bff"],
				[herp: 1, derp: [2, 3]],
				[asdf: [1, 2, 3, 4, 5]]
		    ]
		]
		Map outputValues = [
			found: [ 0, 1, 1, 1, 1, 1, 0 ]*.doubleValue(),
			out: [ null, 1d, 6.66d, "ddd", "Finn", 2, 2 ],
		]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
