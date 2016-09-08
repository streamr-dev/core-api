package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ContainsItemSpec extends Specification {
	ContainsItem module

	def setup() {
		module = new ContainsItem()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "ContainsItem works as expected"() {

		Map inputValues = [
			list: [[], [1,2,3], null, [1, 1], [1, 2, 3, 4, 5, 6]],
		    item:  [6,    null,    2,      6,               null]
		]

		Map outputValues = [
			found: [false, false, true, false, true],
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
