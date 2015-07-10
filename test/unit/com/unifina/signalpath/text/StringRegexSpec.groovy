package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringRegexSpec extends Specification {
	
	StringRegex module
	
    def setup() {
		module = new StringRegex()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "regex"() {
		module.getInput("pattern").receive(~/([A-Z])\w+/.toString())
		
		when:
		Map inputValues = [
			text: ["test", "secondTest", "thirdTest"].collect {it?.toString()}
		]
		Map outputValues = [
			"match?": [0, 1, 1].collect {it?.doubleValue()}
		]
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
	
	
}
