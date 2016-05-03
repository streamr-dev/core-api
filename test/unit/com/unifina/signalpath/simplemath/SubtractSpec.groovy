package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class SubtractSpec extends ModuleSpecification {
	
	Subtract module
	
    def setup() {
		module = new Subtract()
		module.init()
    }

	void "subtract gives the right answer"() {
		when:
		Map inputValues = [
			A: [10, -5, 2, 0, 0].collect {it?.doubleValue()},
			B: [5, 100, 2, -10, 0].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"A-B": [5, -105, 0, 10, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
