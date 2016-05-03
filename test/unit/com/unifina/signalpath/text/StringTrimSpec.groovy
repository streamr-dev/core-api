package com.unifina.signalpath.text

import com.unifina.signalpath.ModuleSpecification

import com.unifina.utils.testutils.ModuleTestHelper

class StringTrimSpec extends ModuleSpecification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }
	
	void "StringTrim works properly"() {
		module = new StringTrim()
		module.init()
		when:
		Map inputValues = [
			text: ["      ", "foo      ", "   foo", "   foo    "]
		]
		Map outputValues = [
			trimmedText: ["", "foo", "foo", "foo"]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
