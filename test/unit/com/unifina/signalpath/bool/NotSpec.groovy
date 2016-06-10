package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class NotSpec extends ModuleSpecification {
	
	Not module
	
    def setup() {
		module = new Not()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "not gives the right answer"() {
		when:
		Map inputValues = [
			in: [false, true],
		]
		Map outputValues = [
			out: [true, false]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
