package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VarianceSpec extends Specification {

	Variance module

	def setup() {
		module = new Variance()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "2"]
		]])
	}

	void "variance gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 1, 0, -1, 13.329316686].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 0, 0.33333333, 1, 8**2].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
