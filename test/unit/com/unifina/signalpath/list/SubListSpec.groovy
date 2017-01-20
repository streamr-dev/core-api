package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SubListSpec extends Specification {
	SubList module

	def setup() {
		module = new SubList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "SubList error cases work as expected"() {
		Map inputValues = [
			from: [0, -1,  1],
			to:   [1,  0,  0],
		    in:   [[], [], []],
		]

		Map outputValues = [
			error: ["toIndex = 1", "fromIndex = -1", "fromIndex(1) > toIndex(0)"],
			out: [null, null, null]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "SubList works as expected"() {
		Map inputValues = [
			from: [0,      4,     5,    5,      0,],
			to:   [0,      8,     5,    6,     10,],
			in:   [[], 1..10, 1..10, 1..10, 1..10,]*.toList(),
		]

		Map outputValues = [
			error: [null, null, null, null, null],
			out: [[], 5..8, [], [6], 1..10]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
