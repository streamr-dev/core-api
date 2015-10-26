package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class EqualsSpec extends Specification {
	
	Equals module
	
    def setup() {
		module = new Equals()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "equals gives the right answer"() {
		when:
		Map inputValues = [
			A: [5, 3, 9, -5].collect {it?.doubleValue()},
			B: [5, 8, -4, -5].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [1, 0, 0, 1].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
