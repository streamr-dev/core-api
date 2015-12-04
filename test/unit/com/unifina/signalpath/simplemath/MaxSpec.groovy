package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MaxSpec extends Specification {
	
	Max module
	
    def setup() {
		module = new Max()
		module.init()
    }

	void "max gives the right answer"() {
		when:
		Map inputValues = [
			A: [0, 5, -30, 0].collect {it?.doubleValue()},
			B: [0, 1, 4, -6].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 5, 4, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
