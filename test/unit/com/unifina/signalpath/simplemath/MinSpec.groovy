package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MinSpec extends Specification {
	
	Min module
	
    def setup() {
		module = new Min()
		module.init()
    }

	void "min gives the right answer"() {
		when:
		Map inputValues = [
			A: [0, 5, -30, 0].collect {it?.doubleValue()},
			B: [0, 1, 4, -6].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 1, -30, -6].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
