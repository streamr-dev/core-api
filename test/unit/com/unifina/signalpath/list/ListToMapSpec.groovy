package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ListToMapSpec extends Specification {
	ListToMap module

	def setup() {
		module = new ListToMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "ListToMap works as expected"() {

		Map inputValues = [
		    list: [[], [1,2,3,4], ["a", "b", "c"]],
		]

		Map outputValues = [
			map: [[:], [0d: 1, 1d: 2, 2d: 3, 3d: 4], [0d: "a", 1d: "b", 2d: "c"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
