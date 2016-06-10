package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class AndSpec extends ModuleSpecification {
	
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
			A: [false, false, true, true],
			B: [false, true, false, true]
		]
		Map outputValues = [
			out: [false, false, false, true]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
