package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MinSlidingSpec extends Specification {

	MinSliding module

	def setup() {
		module = new MinSliding()
		module.init()
	}

	void "minSliding gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3)
		module.getInput("minSamples").receive(2)
		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8, 2, 5].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 1, 1, 1.5, 1.5, 6, 7, 2, 2].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
