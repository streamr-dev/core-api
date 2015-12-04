package com.unifina.signalpath.time

import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals

@TestMixin(GrailsUnitTestMixin)
class TimeBetweenEventsSpec extends Specification {
	
	Globals globals
	TimeBetweenEvents module
	
    def setup() {
		globals = new Globals([:], grailsApplication, new SecUser(timezone:"Europe/Helsinki", username: "username"))
		module = new TimeBetweenEvents()
		module.globals = globals
		module.init()
    }

	void "timeBetweenEvents gives the right answer"() {
		when:
		Map inputValues = [
			in: ["a", "b", 4, new Object(), "g", "HH"]
		]
		Map outputValues = [
			ms: [null, 100, 100, 100, 100, 100].collect { it?.doubleValue() },
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration(100)
			.test()
	}
	
	void "must send no output before two subsequent events"() {
		when: "first value is received"
		globals.time = new Date(1436882043000)
		module.getInput("in").receive(1)
		module.sendOutput()
		then: "nothing is sent out"
		module.getOutput("ms").getValue() == null
		
		when: "a second value is received"
		globals.time = new Date(1436882044000)
		module.getInput("in").receive(2)
		module.sendOutput()
		then: "the time is sent out"
		module.getOutput("ms").getValue() == 1000
		
		when: "a third value is received"
		globals.time = new Date(1436882054000)
		module.getInput("in").receive(3)
		module.sendOutput()
		then: "the time is sent out"
		module.getOutput("ms").getValue() == 10000
	}
	
}
