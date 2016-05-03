package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class SpearmansRankCorrelationSpec extends ModuleSpecification {

	SpearmansRankCorrelation module

	def setup() {
		module = new SpearmansRankCorrelation()
		module.init()
	}

	void "spearmansRankCorrelation gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3)

		Map inputValues = [
			inX: [9, 15, 4, -666, -12, 0.25, 1].collect {it?.doubleValue()},
			inY: [-3, 2, 33, 0, 12, 100, -3].collect {it?.doubleValue()},
		]
		Map outputValues = [
			corr: [null, null, -0.5, 0.5, 1, 1, -0.5].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
