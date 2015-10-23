package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class NegateSpec extends Specification {
	
	Negate module
	
    def setup() {
		module = new Negate()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "negate gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, 1, -1, -9.5, 9.5, -3.141592].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"out": [0, -1, 1, 9.5, -9.5, 3.141592].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
