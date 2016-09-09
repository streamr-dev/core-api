package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FlattenListSpec extends Specification {
	FlattenList module

	def setup() {
		module = new FlattenList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "FlattenList works as expected"() {

		Map inputValues = [
			deep: [false, null,  null, true, null, null, null, null, null, null],
			in:   [
				[],
				[1, 2],
				[1, [2, 3], [4, [5, 6, [7, 8]]], 9],
				[1, [2, 3], [4, [5, 6, [7, 8]]], 9],
				[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[666]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]],
				[1, 2, 3, 4],
				[1, 2, 3, 4, [5, 6]],
				[],
				[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]],
				[1, [2, [3, [4, 5], 6, [7, 8]], 9, [10, [11, 12, [13]]], 14], 15],
			],
		]

		Map outputValues = [
			out: [
				[],
				[1, 2],
				[1, 2, 3, 4, [5, 6, [7, 8]], 9],
				1..9,
				[666],
				1..4,
				1..6,
				[],
				[],
				1..15
			],
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
