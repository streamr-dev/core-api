package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class ToUpperCaseSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }
	
	void "ToUpperCase works properly"() {
		module = new ToUpperCase()
		module.init()
		when:
		Map inputValues = [
			text: ["FOO", "BaH", "foobah"]
		]
		Map outputValues = [
			upperCaseText: ["FOO", "BAH", "FOOBAH"]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
