package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class RangeSpec extends Specification {
	Range module

	def setup() {
		module = new Range()
		module.init()
		module.configure(module.getConfiguration())
		module.getInput("from").drivingInput = true
		module.getInput("to").drivingInput = true
		module.getInput("step").drivingInput = true
	}

	def "Range works as expected"() {
		Map inputValues = [
		    from: [1, null, null, null, null,    2, null,   -5, null,     0]*.doubleValue(),
			to:   [1, null, null,    2, null,    1, null,    5, null,  -600]*.doubleValue(),
			step: [1,    5, 0.25, null, -0.3, null,  0.3, -0.5,    0,  -100]*.doubleValue()
		]

		Map outputValues = [
			out: [
				[1],
				[1],
				[1],
				[1, 1.25, 1.5, 1.75, 2],
				[1, 1.3, 1.6, 1.9000000000000001],
				[2, 1.7, 1.4, 1.0999999999999999],
				[2, 1.7, 1.4, 1.0999999999999999],
				[-5, -4.5, -4, -3.5, -3, -2.5, -2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5],
				[],
				[0, -100, -200, -300, -400, -500, -600]
			].collect { it*.doubleValue() }
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.test()
	}
}
