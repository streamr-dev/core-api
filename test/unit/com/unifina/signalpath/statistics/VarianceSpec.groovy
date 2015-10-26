package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VarianceSpec extends Specification {

	StandardDeviation module

	def setup() {
		module = new StandardDeviation()
		module.init()
	}

	void "standardDeviation gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3)
		module.getInput("minSamples").receive(2)

		Map inputValues = [
			in: [1, 1, 0, -1, 13.32931668].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 0, 0.57735027, 1, 8].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
