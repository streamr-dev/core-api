package com.unifina.signalpath.utils

import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class RateLimitSpec extends Specification {
	
	Globals globals
	RateLimit module

    def setup() {
		globals = new Globals([:], grailsApplication, new SecUser(timezone:"Europe/Helsinki", username: "username"))
		module = new RateLimit()
		module.globals = globals
		module.init()
    }

	def setTimeAndValue(Long time, value) {
		globals.time = new Date(time)
		module.getInput("in").receive(value)
		module.sendOutput()
	}

	void "the module lets messages through if the rate is not limited"() {
		setup:
		module.getInput("timeInMillis").receive(1000)
		module.getInput("rate").receive(1)

		when: "first message sent"
		setTimeAndValue(0, "value")

		then: "the value gets through"
		module.getOutput("out").getValue() == "value"

		when: "second message is sent just far enough from the first one"
		setTimeAndValue(1000, "value2")

		then: "the value gets through"
		module.getOutput("out").getValue() == "value2"

		when: "third message is sent way far enough from the second one"
		setTimeAndValue(3000, "value3")

		then: "the value gets through"
		module.getOutput("out").getValue() == "value3"
	}

	void "the module doesn't let message through if there are too many messages is set time"() {
		setup:
		module.getInput("timeInMillis").receive(1000)
		module.getInput("rate").receive(2)

		when: "first message sent"
		setTimeAndValue(0, "value")

		then: "the value gets through"
		module.getOutput("out").getValue() == "value"

		when: "second message is sent"
		setTimeAndValue(300, "value2")

		then: "the value gets through"
		module.getOutput("out").getValue() == "value2"

		when: "third message is sent too close to the first one"
		setTimeAndValue(600, "value3")

		then: "the value doesn't get through"
		module.getOutput("out").getValue() == "value2"

		when: "fourth message is sent far away from the first one"
		setTimeAndValue(1300, "value4")

		then: "the value gets through"
		module.getOutput("out").getValue() == "value4"
	}

	void "the module outputs right values to the limitExceeded? output"() {
		setup:
		module.getInput("timeInMillis").receive(1000)
		module.getInput("rate").receive(2)

		when: "first message sent"
		setTimeAndValue(0, "value")

		then: "not limited"
		module.getOutput("limitExceeded?").getValue() == 0

		when: "second message is sent"
		setTimeAndValue(300, "value2")

		then: "not limited"
		module.getOutput("limitExceeded?").getValue() == 0

		when: "third message is sent too close to the first one"
		setTimeAndValue(600, "value3")

		then: "limited"
		module.getOutput("limitExceeded?").getValue() == 1

		when: "fourth message is sent far away from the first one"
		setTimeAndValue(1300, "value4")

		then: "not limited"
		module.getOutput("limitExceeded?").getValue() == 0
	}

	void "the module works correctly with a bigger and different input"() {
		setup:
		module.getInput("timeInMillis").receive(100)
		module.getInput("rate").receive(2)

		def timeToFurther = [60, 50, 30, 10, 70]

		Map inputValues = [
				in: [1, 2, 3, 4, 5, 6].collect { it?.doubleValue() }
		]

		Map outputValues = [
				"out": [1, 2, 3, 3, 3, 6].collect { it?.doubleValue() },
				"limitExceeded?": [0, 0, 0, 1, 1, 0].collect { it?.doubleValue() },
		]

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.timeToFurtherPerIteration(timeToFurther)
				.test()
	}

	void "the module works with a solid time between the events"() {
		setup:
		module.getInput("timeInMillis").receive(100)
		module.getInput("rate").receive(1)

		Map inputValues = [
				in: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() }
		]

		Map outputValues = [
				"out": [1, 1, 1, 1, 5, 5, 5, 5, 9, 9].collect { it?.doubleValue() },
				"limitExceeded?": [0, 1, 1, 1, 0, 1, 1, 1, 0, 1].collect { it?.doubleValue() },
		]

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.timeToFurtherPerIteration(30)
				.test()
	}

	void "if rate is 0 no values should be sent out"() {
		setup:
		globals.time = new Date(0)
		module.getInput("timeInMillis").receive(100)
		module.getInput("rate").receive(0)

		def timeToFurther = [0, 0, 1, 2, 3, 10, 20, 100, 1000, 1000].collect { it?.intValue() }

		Map inputValues = [
				in: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() }
		]

		Map outputValues = [
				"out": [null, null, null, null, null, null, null, null, null, null],
				"limitExceeded?": [1, 1, 1, 1, 1, 1, 1, 1, 1, 1].collect { it?.doubleValue() },
		]

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.timeToFurtherPerIteration(timeToFurther)
				.test()
	}

	void "if time is 0 every value should be sent out"() {
		setup:
		globals.time = new Date(0)
		module.getInput("timeInMillis").receive(0)
		module.getInput("rate").receive(1)

		def timeToFurther = [0, 0, 1, 2, 3, 10, 20, 100, 1000, 1000].collect { it?.intValue() }

		Map inputValues = [
				in: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() }
		]

		Map outputValues = [
				"out": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].collect { it?.doubleValue() },
				"limitExceeded?": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0].collect { it?.doubleValue() },
		]

		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.timeToFurtherPerIteration(timeToFurther)
				.test()
	}
}
