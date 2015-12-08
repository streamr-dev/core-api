package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SquareRootSpec extends Specification {
	
	SquareRoot module
	
    def setup() {
		module = new SquareRoot()
		module.init()
    }

	void "squareRoot gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, 1, -1, 16, 144].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"sqrt": [0, 1, 0, 4, 12].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
