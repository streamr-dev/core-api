package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SortMapSpec extends Specification {
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

	def "sortMap() sorts map by key, ascending"() {
		module.getInput("by").receive(SortMap.ByParameter.KEY)
		module.getInput("order").receive(SortMap.OrderParameter.ASCENDING)

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

	def "sortMap() sorts map by value, ascending"() {
		module.getInput("by").receive(SortMap.ByParameter.VALUE)
		module.getInput("order").receive(SortMap.OrderParameter.ASCENDING)
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

	def "sortMap() sorts map by key, descending"() {
		module.getInput("by").receive(SortMap.ByParameter.KEY)
		module.getInput("order").receive(SortMap.OrderParameter.DESCENDING)
		Map outputValues = [
			out: [
				[:],
				[z: "z", d: "d", c: "c", b: "b", a: "a"],
				[z: 4, d: 8, c: 0, b: 1, a: 5]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "sortMap() sorts map by value, descending"() {
		module.getInput("by").receive(SortMap.ByParameter.VALUE)
		module.getInput("order").receive(SortMap.OrderParameter.DESCENDING)
		Map outputValues = [
			out: [
				[:],
				[z: "z", d: "d", c: "c", b: "b", a: "a"],
				[d: 8, a: 5, z: 4, b: 1, c: 0]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "sortMap() does not crash given given uncomparable values"() {
		module.getInput("by").receive(SortMap.ByParameter.VALUE)
		module.getInput("order").receive(SortMap.OrderParameter.ASCENDING)

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
