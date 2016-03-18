package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FlexBarifySpec extends Specification {

	FlexBarify module

	def setup() {
		module = new FlexBarify()
		module.init()
		module.configure([:])
	}

	void "flexBarify gives the right answer"() {
		when:
		module.getInput("barLength").receive(60)
		Map inputValues = [
			valueLength: [
				20, 20, 20,
				10, 10, 10, 10, 10, 10,
			].collect { it?.doubleValue() },
			value: [
				3, 3.5, 3.2,
				2.8, 2.1, 2.0, 3.05, 3.11, 2.99].collect { it?.doubleValue() },
		]
		Map outputValues = [
			open : [null, null, 3, 3, 3, 3, 3, 3, 3.2].collect { it?.doubleValue() },
			high : [null, null, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.2].collect { it?.doubleValue() },
			low  : [null, null, 3, 3, 3, 3, 3, 3, 2.0].collect { it?.doubleValue() },
			close: [null, null, 3.2, 3.2, 3.2, 3.2, 3.2, 3.2, 2.99].collect { it?.doubleValue() },
			sum  : [null, null, 130, 130, 130, 130, 130, 130, 194.6].collect { it?.doubleValue() },
			avg  : [null, null, 3.25, 3.25, 3.25, 3.25, 3.25, 3.25, 2.78].collect { it?.doubleValue() },

		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
