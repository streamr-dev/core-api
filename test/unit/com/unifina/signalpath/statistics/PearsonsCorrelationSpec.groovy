package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PearsonsCorrelationSpec extends Specification {

	PearsonsCorrelation module

	def setup() {
		module = new PearsonsCorrelation()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "3"]
		]])
	}

	void "pearsonsCorrelation gives the right answer"() {
		when:
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
