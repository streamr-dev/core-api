package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class IfThenElseSpec extends Specification {
	
	IfThenElse module
	
    def setup() {
		module = new IfThenElse()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "ifThenElse gives the right answer"() {
		when:
		Map inputValues = [
			if: [true, true, false, false],
			then: [1, 2, 5, 6].collect {it?.doubleValue()},
			else: [7, 8, 3, 4].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [1, 2, 3, 4].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
