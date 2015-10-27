package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class ToLowerCaseSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }
	
	void "ToLowerCase works properly"() {
		module = new ToLowerCase()
		module.init()
		when:
		Map inputValues = [
			text: ["FOO", "BaH", "foobah"]
		]
		Map outputValues = [
			lowerCaseText: ["foo", "bah", "foobah"]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
