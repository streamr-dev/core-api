package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class LessThanSpec extends Specification {
	
	LessThan module
	
    def setup() {
		module = new LessThan()
		module.init()
		module.configure([:])
    }

    def cleanup() {
		
    }
	
	void "lessThan gives the right answer"() {
		when:
		Map inputValues = [
			A: [5, 3, 9].collect {it?.doubleValue()},
			B: [5, 8, -4].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"A&lt;B": [false, true, false]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "lessThan with equality turned on gives the right answer"() {
		module.getInput("equality").receive(Boolean.TRUE)
		when:
		Map inputValues = [
			A: [5, 3, 9].collect { it?.doubleValue() },
			B: [5, 8, -4].collect { it?.doubleValue() },
		]
		Map outputValues = [
			"A&lt;B": [true, true, false]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
