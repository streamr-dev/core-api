package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class DelaySpec extends Specification {

	Delay module

	def setup() {
		module = new Delay()
		module.init()
	}

	void "delay gives the right answer"() {
		when:
		module.getInput("delayEvents").receive(3)
		Map inputValues = [
			in: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() },
		]
		Map outputValues = [
			out : [null, null, null, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.extraIterationsAfterInput(3)
			.test()
	}
}
