package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringSplitSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }
	
	void "StringSplit works properly"() {
		module = new StringSplit()
		module.init()
		module.getInput("separator").receive(" ")
		when:
		Map inputValues = [
			text: ["foo bah", " foo", "bah", " "]
		]
		Map outputValues = [
			"list": [["foo", "bah"], ["", "foo"], ["bah"], []]
		]
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
