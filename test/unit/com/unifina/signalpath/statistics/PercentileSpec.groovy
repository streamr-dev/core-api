package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PercentileSpec extends Specification {

	Percentile module

	def setup() {
		module = new Percentile()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "2"],
				[name: "percentage", value: 75D]
		]])
	}

	void "percentile gives the right answer"() {
		when:
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
