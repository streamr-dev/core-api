package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PercentileSpec extends Specification {

	Percentile module

	def setup() {
		module = new Percentile()
		module.init()
	}

	void "percentile gives the right answer"() {
		when:
		module.getInput("windowLength").receive(5)
		module.getInput("minSamples").receive(2)
		module.getInput("percentage").receive(75D)

		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 3, 3, 5.25, 6.5, 19, 19.5].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
