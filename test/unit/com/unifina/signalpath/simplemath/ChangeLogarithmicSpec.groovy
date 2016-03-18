package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ChangeLogarithmicSpec extends Specification {
	
	ChangeLogarithmic module
	
    def setup() {
		module = new ChangeLogarithmic()
		module.init()
    }

	void "changeLogarithmic gives the right answer"() {
		when:
		Map inputValues = [
			in: [1, Math.exp(5), Math.exp(20), Math.exp(100), Math.exp(1), -10].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"out": [null, 5, 15, 80, -99, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
