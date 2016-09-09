package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SortListSpec extends Specification {
	SortList module

	def setup() {
		module = new SortList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "SortList works as expected"() {

		Map inputValues = [
			order: ["asc", "desc",                        null, "asc"],
		    in:    [   [],     [], [4, 9, 2, 1, 5, 7, 8, 3, 6], [4, 9, 2, 1, 5, 7, 8, 3, 6]]
		]

		Map outputValues = [
			out: [[], [], 9..1, 1..9]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
