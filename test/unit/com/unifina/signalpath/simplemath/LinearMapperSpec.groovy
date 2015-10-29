package com.unifina.signalpath.simplemath

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class LinearMapperSpec extends Specification {
	
	LinearMapper module
	
    def setup() {
		module = new LinearMapper()
		module.init()
		module.configure([params: [
		    [name: "xMin", value: -0.5],
			[name: "xMax", value: 1],
			[name: "yMin", value: 0],
			[name: "yMax", value: 1],
		]])
    }

	void "linearMapper gives the right answer"() {
		when:
		Map inputValues = [
			in: [0, -0.3, 0.3, 0.5, 1, 13, -13, -0.5, -4, -1, 0.3].collect {it?.doubleValue()},
		]
		Map outputValues = [
			"out": [0.33333333, 0.13333333, 0.53333333, 0.66666667, 1, 1, 0, 0, 0, 0, 0.53333333].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
