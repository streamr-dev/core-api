package com.unifina.signalpath.simplemath

import com.unifina.utils.DU
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ChangeRelativeSpec extends Specification {
	
	ChangeRelative module
	
    def setup() {
		module = new ChangeRelative()
		module.init()
    }

	void "changeRelative gives the right answer"() {
		when:
		Map inputValues = [
			in: [100, 500, -1000, 0, 666, 665].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"out": [null, 4, -3, -1, -1, DU.clean(-1 / 666)].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
