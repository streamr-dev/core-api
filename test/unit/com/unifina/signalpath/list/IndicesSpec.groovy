package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class IndicesSpec extends Specification {
	Indices module

	def setup() {
		module = new Indices()
		module.init()
		module.configure(module.configuration)
	}

	def "EachWithIndex works as expected"() {
		Map inputValues = [
			list: [
			    [],
				[2],
				["abba", "jabba", 777, "ddd"],
				["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"],
				[]
			]
		]

		Map outputValues = [
			list: [
				[],
				[2],
				["abba", "jabba", 777, "ddd"],
				["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"],
				[]
			],
			indices: [[], [0], 0..3, 0..10, []]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
