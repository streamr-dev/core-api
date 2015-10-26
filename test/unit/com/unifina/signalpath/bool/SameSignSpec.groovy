package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SameSignSpec extends Specification {
	
	SameSign module
	
    def setup() {
		module = new SameSign()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "sameSign gives the right answer"() {
		when:
		Map inputValues = [
			A: [0, -0, 0, 0, 2, 2, -4].collect {it?.doubleValue()},
			B: [0, +0, -3, 3, -2, 5, -7].collect {it?.doubleValue()},
		]
		Map outputValues = [
			sign: [0, 0, 0, 0, 0, 1, -1].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
