package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class SkewnessSpec extends ModuleSpecification {

	Skewness module

	def setup() {
		module = new Skewness()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "4"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "3"]
		]])
	}

	void "skewness gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, null, 1.29334278, 1.24828532, -0.15800287, 1.8011211, 1.97239806].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
