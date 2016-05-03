package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class LessThanSpec extends ModuleSpecification {
	
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
			"A&lt;B": [0, 1, 0].collect {it?.doubleValue()}
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
			"A&lt;B": [1, 1, 0].collect { it?.doubleValue() }
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
