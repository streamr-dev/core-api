package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class AndSpec extends Specification {
	
	And module
	
    def setup() {
		module = new And()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "and gives the right answer"() {
		when:
		Map inputValues = [
			A: [0, 0, 1, 1].collect {it?.doubleValue()},
			B: [0, 1, 0, 1].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 0, 0, 1].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
