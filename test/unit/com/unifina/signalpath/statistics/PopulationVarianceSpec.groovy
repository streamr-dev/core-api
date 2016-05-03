package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class PopulationVarianceSpec extends ModuleSpecification {

	PopulationVariance module

	def setup() {
		module = new PopulationVariance()
		module.init()
	}

	void "populationVariance gives the right answer"() {
		when:
		module.getInput("windowLength").receive(4)
		module.getInput("minSamples").receive(2)

		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 1, 0.72222222, 3.796875, 4.921875, 132.671875, 108.5].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
