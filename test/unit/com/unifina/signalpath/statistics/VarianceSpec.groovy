package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VarianceSpec extends Specification {

	Variance module

	def setup() {
		module = new Variance()
		module.init()
	}

	void "variance gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3)
		module.getInput("minSamples").receive(2)

		Map inputValues = [
			in: [1, 1, 0, -1, 13.329316686].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 0, 0.33333333, 1, 8**2].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
