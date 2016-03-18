package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ChangeAbsoluteSpec extends Specification {
	
	ChangeAbsolute module
	
    def setup() {
		module = new ChangeAbsolute()
		module.init()
    }

	void "changeAbsolute gives the right answer"() {
		when:
		Map inputValues = [
			in: [5, 4, -4, 0, 2, -5, 7].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"out": [null, -1, -8, 4, 2, -7, 12].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
