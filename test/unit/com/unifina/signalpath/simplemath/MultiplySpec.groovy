package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MultiplySpec extends Specification {
	
	Multiply module
	
    def setup() {
		module = new Multiply()
		module.init()
    }

	void "multiply gives the right answer"() {
		when:
		Map inputValues = [
			A: [10, -5, 2, 0, 0].collect {it?.doubleValue()},
			B: [5, 100, 2, -10, 0].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"A*B": [50, -500, 4, 0, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
