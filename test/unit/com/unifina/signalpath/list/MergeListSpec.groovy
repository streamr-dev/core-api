package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MergeListSpec extends Specification {
	MergeList module

	def setup() {
		module = new MergeList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "MergeList works as expected"() {

		Map inputValues = [
			head: [[], [1, 2, 3],     [], [1, 2, 3], ["hello"]],
			tail: [[],      null, [4, 5],      null, ["world", "!"]]
		]

		Map outputValues = [
			out: [[], [1, 2, 3], [4, 5], 1..5, ["hello", "world", "!"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
