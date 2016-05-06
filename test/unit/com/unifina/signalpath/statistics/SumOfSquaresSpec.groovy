package com.unifina.signalpath.statistics

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SumOfSquaresSpec extends Specification {

	SumOfSquares module

	def setup() {
		module = new SumOfSquares()
		module.init()
		module.configure([inputs: [
				[name: "windowLength", value: "3"],
				[name: "windowType", value: "EVENTS"],
				[name: "minSamples", value: "2"]
		]])
	}

	void "sumOfSquares gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, 1, 0, -1, 2, -5, 13].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [null, 2, 2, 2, 5, 30, 198].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
