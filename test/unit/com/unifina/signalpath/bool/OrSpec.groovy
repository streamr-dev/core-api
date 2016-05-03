package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class OrSpec extends ModuleSpecification {
	
	Or module
	
    def setup() {
		module = new Or()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "or gives the right answer"() {
		when:
		Map inputValues = [
			A: [0, 0, 1, 1].collect {it?.doubleValue()},
			B: [0, 1, 0, 1].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [0, 1, 1, 1].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
