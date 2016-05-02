package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class BarifySpec extends Specification {

	Barify module

	def setup() {
		module = new Barify()
		module.init()
	}

	void "barify gives the right answer"() {
		when:
		module.getInput("barLength").receive(60)
		Map inputValues = [
			in: [3, 3.5,
				 3.2, 2.8, 2.1, 2.0, 3.05, 3.11,
				 2.99, 2.59, 3, 3.1, 3.2, 0.995, 4.1, 3.2,
				 3.3
			].collect { it?.doubleValue() },
		]
		Map outputValues = [
			open : [3, 3.5, 3.11].collect { it?.doubleValue() },
			high : [3.5, 3.5, 4.1].collect { it?.doubleValue() },
			low  : [3, 2.0, 0.995].collect { it?.doubleValue() },
			close: [3.5, 3.11, 3.2].collect { it?.doubleValue() },
			sum  : [6.5, 16.26, 23.175].collect { it?.doubleValue() },
			avg  : [3.25, 2.71, 2.896875].collect { it?.doubleValue() },

		]
		Map ticks = [
			2: new Date(60 * 1000),
			8: new Date(60 * 3000),
			16: new Date(60 * 17000),
		]


		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.ticks(ticks)
			.test()
	}
}
