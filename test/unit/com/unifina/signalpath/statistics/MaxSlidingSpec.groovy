package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MaxSlidingSpec extends Specification {

	MaxSliding module

	def setup() {
		module = new MaxSliding()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "2"]
		]])
	}

	void "maxSliding gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 3, 1.5, 6, 7, 31, 8, 2, 5].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 3, 3, 6, 7, 31, 31, 31, 8].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
