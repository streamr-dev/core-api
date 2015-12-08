package com.unifina.signalpath.time

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class TimeOfDaySpec extends Specification {

	TimeOfDay module

	def setup(){
		module = new TimeOfDay()
		module.init()
	}

	void "timeOfDay gives the right answer"() {
		when:
		Map inputValues = [:]
		Map outputValues = [
			out: [0, 1, 1, 1, 0].collect { it?.doubleValue() }
		]
		Map ticks = [
		    0: Date.parse("yyyy-MM-dd HH:mm:ss", "2015-10-31 23:30:30"),
			1: Date.parse("yyyy-MM-dd HH:mm:ss", "2015-11-01 03:00:00"),
			2: Date.parse("yyyy-MM-dd HH:mm:ss", "2015-11-01 17:45:00"),
			3: Date.parse("yyyy-MM-dd HH:mm:ss", "2015-11-01 23:59:59"),
			4: Date.parse("yyyy-MM-dd HH:mm:ss", "2015-11-02 02:00:00"),
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.ticks(ticks)
			.extraIterationsAfterInput(5)
			.overrideGlobals { g ->
				g.init()
				g
			}
			.beforeEachTestCase { module.onDay(Date.parse("yyyy-MM-dd HH:mm", "2015-11-01 12:00")) }
			.test()
	}
}
