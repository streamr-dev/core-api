package com.unifina.signalpath.utils.testutils

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.utils.testutils.MultiModule
import spock.lang.Specification

class MultiModuleSpec extends Specification {

	MultiModule module

	def setup() {
		module = new MultiModule()
		module.init()
	}

	void "multiModule gives the right answer"() {
		when:
		Map inputValues = [
			in1: [1, 5, 8, 4, 10, 13, -21].collect { it?.doubleValue() },
			in2: [0, 3, 13, 9, 2, 1, 5].collect { it?.doubleValue() },
		]
		Map outputValues = [
			out: [1, 1.559017, 2.07409578, 2.99717274, 4.31478838, 5.86002461, 4.88174487].collect { it?.doubleValue() },
		]


		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
