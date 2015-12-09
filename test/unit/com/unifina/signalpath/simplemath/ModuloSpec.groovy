package com.unifina.signalpath.simplemath

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class ModuloSpec extends Specification {
	
	Modulo module
	
    def setup() {
		module = new Modulo()
		module.init()
    }

	void "modulo gives the right answer"() {
		module.getInput("divisor").receive(5)		
		when:
		Map inputValues = [
			dividend: [0, 1, 2, 9, 10, 11, 12, 13, 14, 15].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"remainder": [0, 1, 2, 4, 0, 1, 2, 3, 4, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
