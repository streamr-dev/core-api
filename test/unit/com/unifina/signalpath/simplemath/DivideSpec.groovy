package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class DivideSpec extends Specification {
	
	Divide module
	
    def setup() {
		module = new Divide()
		module.init()
    }

	void "divide gives the right answer"() {
		when:
		Map inputValues = [
			A: [10, -5, 2, 0, 0].collect {it?.doubleValue()},
			B: [5, 100, 2, -10, 0].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"A/B": [2, -0.05, 1, 0, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
