package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class NotSpec extends Specification {
	
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
			in: [0, 1].collect {it?.doubleValue()},
		]
		Map outputValues = [
			out: [1, 0].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
