package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class CountSpec extends ModuleSpecification {
	
	Count module
	
    def setup() {
		module = new Count()
		module.init()
    }

	void "count gives the right answer"() {
		when:
		Map inputValues = [
			in: ["", "", "", "", ""],
		]
		Map outputValues = [
			"count": [1, 2, 3, 4, 5].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
