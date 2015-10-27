package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SignSpec extends Specification {
	
	Sign module
	
    def setup() {
		module = new Sign()
		module.init()
    }

	void "sign gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, 1, -1, -9.5, 9.5, -3.141592, 0].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"out": [0, 1, -1, -1, 1, -1, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
