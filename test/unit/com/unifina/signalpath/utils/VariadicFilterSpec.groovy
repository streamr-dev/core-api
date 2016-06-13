package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VariadicFilterSpec extends Specification {

	VariadicFilter module

	def setup() {
		module = new VariadicFilter()
		module.init()
		module.getInput("endpoint-352323")
		module.getInput("endpoint-not-used-5234523")
		module.getOutput("endpoint-532135")
		module.getOutput("endpoint-not-used-1231231")
		module.configure([:])
	}

	void "filter gives the right answer"() {
		when:
		Map inputValues = [
			in1: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() },
			pass: [0, 0, 1, 0, 1, 0, 0, 1, 1, 0].collect { it?.doubleValue() },
		]
		Map outputValues = [
			out1: [null, null, 3, 3, 5, 5, 5, 8, 9, 9].collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
