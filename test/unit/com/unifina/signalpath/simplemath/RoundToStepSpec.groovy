package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class RoundToStepSpec extends Specification {

	RoundToStep module

	def setup() {
		module = new RoundToStep()
		module.init()
	}

	void "roundToStep gives the right answer"() {
		when:
		Map inputValues = [

			// Parameter
			mode: [
				RoundToStep.MODE_UP,
				RoundToStep.MODE_DOWN,
				RoundToStep.MODE_TOWARDS_ZERO,
				RoundToStep.MODE_AWAYFROM_ZERO
			].collect {it?.doubleValue()},

			// Parameter
			precision: [
				0.1,
				0.1,
				1,
				1
			].collect {it?.doubleValue()},

			in: [
				0.34,
				0.34,
				-0.8,
				-0.8
			].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"out": [0.4, 0.3, 0, -1].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
