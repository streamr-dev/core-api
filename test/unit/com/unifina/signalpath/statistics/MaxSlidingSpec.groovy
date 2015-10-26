package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MaxSlidingSpec extends Specification {

	MaxSliding module

	def setup() {
		module = new MaxSliding()
		module.init()
	}

	void "maxSliding gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3)
		module.getInput("minSamples").receive(2)
		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8, 2, 5].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 3, 3, 6, 7, 31, 31, 31, 8].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
