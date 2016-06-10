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
			A: [false, false, true, true],
			B: [false, true, false, true]
		]
		Map outputValues = [
			out: [false, true, true, true]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
