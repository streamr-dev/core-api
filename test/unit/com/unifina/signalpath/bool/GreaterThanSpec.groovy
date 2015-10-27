package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class GreaterThanSpec extends Specification {
	
	GreaterThan module
	
    def setup() {
		module = new GreaterThan()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "greaterThan gives the right answer"() {
		when:
		Map inputValues = [
			A: [5, 3, 9].collect {it?.doubleValue()},
			B: [5, 8, -4].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"A&gt;B": [0, 0, 1].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
//
//	void "lessThan with equality turned on gives the right answer"() {
//		module.getInput("equality").receive(Boolean.TRUE)
//		when:
//		Map inputValues = [
//			A: [5, 3, 9].collect { it?.doubleValue() },
//			B: [5, 8, -4].collect { it?.doubleValue() },
//		]
//		Map outputValues = [
//			"A&lt;B": [1, 1, 0].collect { it?.doubleValue() }
//		]
//
//		then:
//		new ModuleTestHelper(module, inputValues, outputValues).test()
//	}
}
