package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ReverseListSpec extends Specification {
	ReverseList module

	def setup() {
		module = new ReverseList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "ReverseList works as expected"() {

		Map inputValues = [
		    in: [[], [1, 2, 3], ["a", "b", ["c", "d"], "e"]]
		]

		Map outputValues = [
			out: [[], [3, 2, 1], ["e", ["c", "d"], "b", "a"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
