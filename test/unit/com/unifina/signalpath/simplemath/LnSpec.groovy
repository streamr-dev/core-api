package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class LnSpec extends ModuleSpecification {
	
	Ln module
	
    def setup() {
		module = new Ln()
		module.init()
    }

	void "ln gives the right answer"() {
		when:
		Map inputValues = [
			in: [Math.exp(1), Math.exp(-50), 1, Math.exp(3.141592)].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [1, -50, 0, 3.141592].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
