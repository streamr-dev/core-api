package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VariadicAddMultiSpec extends Specification {
	
	VariadicAddMulti module
	
    def setup() {
		module = new VariadicAddMulti()
		module.init()
		module.getInput("in3")
		module.getInput("in4")
		module.getInput("in5")
		module.configure([:])
    }

	void "addMulti gives the right answer"() {
		when:
		Map inputValues = [
			in1: [1, 2, 3, 4, 5].collect {it?.doubleValue()},
			in2: [5, 10, 15, 20, 25].collect {it?.doubleValue()},
			in3: [100, 0, 100, 0, 100].collect {it?.doubleValue()},
			in4: [-10, -1, -1000, 0, -5].collect {it?.doubleValue()}
		]
		Map outputValues = [
			"sum": [96, 11, -882, 24, 125].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
