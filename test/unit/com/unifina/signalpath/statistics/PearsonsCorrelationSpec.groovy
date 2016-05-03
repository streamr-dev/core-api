package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class PearsonsCorrelationSpec extends ModuleSpecification {

	PearsonsCorrelation module

	def setup() {
		module = new PearsonsCorrelation()
		module.init()
	}

	void "pearsonsCorrelation gives the right answer"() {
		when:
		module.getInput("windowLength").receive(3)
		Map inputValues = [
			inX: [1, 4, 0.5, 6, 3, 7, 10].collect {it?.doubleValue()},
			inY: [2, 8, 1, 12, 9, 14, 21].collect {it?.doubleValue()},
		]
		Map outputValues = [
			corr: [null, null, 1, 1, 0.95261293, 0.98624138, 0.98416045].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
