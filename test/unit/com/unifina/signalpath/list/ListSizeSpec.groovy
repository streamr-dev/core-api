package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ListSizeSpec extends Specification {
	ListSize module

	def setup() {
		module = new ListSize()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "ListSize works as expected"() {

		Map inputValues = [
		    in: [
		        [], [1, 2, 3], ["a", "b", "c", ["d", "e"]]
		    ],
		]

		Map outputValues = [
			size: [0, 3, 4]*.doubleValue()
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
