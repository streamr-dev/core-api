package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class UniqueSpec extends Specification {
	Unique module

	def setup() {
		module = new Unique()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "Unique works as expected"() {

		Map inputValues = [
		    list: [[], [1, 2, 3], [1, 4, 5, 4, 4, 2, 5, 1, 5, 2], ["a", "b", "a", "b", "c", "c", "c", "b", "d"]],
		]

		Map outputValues = [
			list: [[], [1, 2, 3], [1, 4, 5, 2], ["a", "b", "c", "d"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
