package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FilterSpec extends Specification {

	Filter module

	def setup() {
		module = new Filter()
		module.init()
	}

	void "filter gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() },
			pass: [0, 0, 1, 0, 1, 0, 0, 1, 1, 0].collect { it?.doubleValue() },
		]
		Map outputValues = [
			out : [null, null, 3, 3, 5, 5, 5, 8, 9, 9].collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
