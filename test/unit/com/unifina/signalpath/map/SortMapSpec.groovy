package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class SortMapSpec extends ModuleSpecification {
	SortMap module

	Map inputValues = [
		in: [
			[:],
			[z: "z", b: "b", d: "d", a: "a", c: "c"],
			[z: 4, b: 1, d: 8, a: 5, c: 0],
		]
	]

	def setup() {
		module = new SortMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "sortMap() sorts map (default: by key)"() {
		Map outputValues = [
			out: [
				[:],
				[a: "a", b: "b", c: "c", d: "d", z: "z"],
				[a: 5, b: 1, c: 0, d: 8, z: 4]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "sortMap() sorts map by key (by value)"() {
		module.getInput("byValue").receive(true)
		Map outputValues = [
			out: [
				[:],
				[a: "a", b: "b", c: "c", d: "d", z: "z"],
				[c: 0, b: 1, z: 4, a: 5, d: 8]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "sortMap() does not crash given given uncomparable values"() {
		module.getInput("byValue").receive(true)

		Map inputValues = [
			in: [[a: new Object(), b: new Object(), d: new Object()]]
		]

		Map outputValues = [
			out: [null]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
