package com.unifina.signalpath.time

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class TimeOfEventSpec extends Specification {

	TimeOfEvent module

	def setup() {
		module = new TimeOfEvent()
		module.init()
	}

	void "TimeOfEvent gives the right answer"() {
		when:
		Map inputValues = [
			trigger: (1..7).toList(),
		]
		Map outputValues = [
			timestamp: [0, 10, 15, 17, 17, 18, 1018]*.doubleValue()
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration([10, 5, 2, 0, 1, 1000])
			.test()
	}
}
