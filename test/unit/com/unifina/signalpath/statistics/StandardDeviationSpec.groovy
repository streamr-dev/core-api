package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class StandardDeviationSpec extends Specification {

	StandardDeviation module

	def setup() {
		module = new StandardDeviation()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "2"]
		]])
	}

	void "standardDeviation gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 1, 0, -1, 13.32931668].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 0, 0.57735027, 1, 8].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
