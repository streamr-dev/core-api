package com.unifina.signalpath.utils

import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
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
		module.getInput("input").receive(value)
		module.sendOutput()
	}

	void "the module lets messages through if the rate is not limited"() {
		setup:
		module.getInput("timeInMillis").receive(1000)
		module.getInput("rate").receive(1)

		when: "first message sent"
		setTimeAndValue(1450000000000, "value")

		then: "the value gets through"
		module.getOutput("output").getValue() == "value"

		when: "second message is sent just far enough from the first one"
		setTimeAndValue(1450000001000, "value2")

		then: "the value gets through"
		module.getOutput("output").getValue() == "value2"

		when: "third message is sent way far enough from the second one"
		setTimeAndValue(1450000003000, "value3")

		then: "the value gets through"
		module.getOutput("output").getValue() == "value3"
	}

	void "the module doesn't let message through if there are too many messages is set time"() {
		setup:
		module.getInput("timeInMillis").receive(1000)
		module.getInput("rate").receive(2)

		when: "first message sent"
		setTimeAndValue(1450000000000, "value")

		then: "the value gets through"
		module.getOutput("output").getValue() == "value"

		when: "second message is sent"
		setTimeAndValue(1450000000300, "value2")

		then: "the value gets through"
		module.getOutput("output").getValue() == "value2"

		when: "third message is sent too close to the first one"
		setTimeAndValue(1450000000600, "value3")

		then: "the value doesn't get through"
		module.getOutput("output").getValue() == "value2"

		when: "fourth message is sent far away from the first one"
		setTimeAndValue(1450000001000, "value4")

		then: "the value gets through"
		module.getOutput("output").getValue() == "value4"
	}

	void "the module outputs right values to the limitExceeded? output"() {
		setup:
		module.getInput("timeInMillis").receive(1000)
		module.getInput("rate").receive(2)

		when: "first message sent"
		setTimeAndValue(1450000000000, "value")

		then: "not limited"
		module.getOutput("limitExceeded?").getValue() == 0

		when: "second message is sent"
		setTimeAndValue(1450000000300, "value2")

		then: "not limited"
		module.getOutput("limitExceeded?").getValue() == 0

		when: "third message is sent too close to the first one"
		setTimeAndValue(1450000000600, "value3")

		then: "limited"
		module.getOutput("limitExceeded?").getValue() == 1

		when: "fourth message is sent far away from the first one"
		setTimeAndValue(1450000001000, "value4")

		then: "not limited"
		module.getOutput("limitExceeded?").getValue() == 0
	}

	void "the module works correctly with a bigger and different input"() {
		setup:
		module.getInput("timeInMillis").receive(100)
		module.getInput("rate").receive(5)
		def times = [1450000000010, 1450000000020, 145000000030, 1450000000040, 1450000000050, 1450000000060, 1450000000130, 1450000000140, 1450000000145, 14500000000160]
		def values = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
		def outputValues = [1, 2, 3, 4, 5, 5, 7, 8, 8, 10]
		def limitExceededValues = [0, 0, 0, 0, 0, 1, 0, 0, 1, 0]

		for(int i = 0; i < 10; i++) {
			when: "set time and value found from times[i] and values[i]"
			setTimeAndValue(times[i], values[i])

			then: "outputs found from the outputValues[i] and limitExceededValues[i] are correct"
			module.getOutput("output").getValue() == outputValues[i]
			module.getOutput("limitExceeded?").getValue() == limitExceededValues[i]
		}
	}
}
