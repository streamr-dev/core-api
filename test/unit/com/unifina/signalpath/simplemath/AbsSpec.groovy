package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class AbsSpec extends Specification {
	
	Abs module
	
    def setup() {
		module = new Abs()
		module.init()
    }

	void "abs gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, 1, -1, -9.5, 9.5, -3.141592].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"out": [0, 1, 1, 9.5, 9.5, 3.141592].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
