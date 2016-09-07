package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class RemoveFromListSpec extends Specification {
	RemoveFromList module

	def setup() {
		module = new RemoveFromList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "RemoveFromList works as expected"() {

		Map inputValues = [
			index: [2, null, 0, 3, 666, -1, -4, -666],
			in: [
				[],
				["a", "b", "c", "d"],
				["a", "b", "c", "d"],
				["a", "b", "c", "d"],
				["a", "b", "c", "d"],
				["a", "b", "c", "d"],
				["a", "b", "c", "d"],
				["a", "b", "c", "d"],
			]
		]

		Map outputValues = [
			out: [
				[],
				["a", "b", "d"],
				["b", "c", "d"],
				["a", "b", "c"],
				["a", "b", "c", "d"],
				["a", "b", "c"],
				["b", "c", "d"],
				["a", "b", "c", "d"],
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
