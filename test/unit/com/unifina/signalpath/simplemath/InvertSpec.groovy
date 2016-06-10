package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class InvertSpec extends ModuleSpecification {
	
	Invert module
	
    def setup() {
		module = new Invert()
		module.init()
    }

	void "invert gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, -1, 100, 5000, 1/131072].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"out": [1, -1, 0.01, 0.0002, 131072].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
