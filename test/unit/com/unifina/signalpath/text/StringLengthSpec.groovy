package com.unifina.signalpath.text

import com.unifina.signalpath.ModuleSpecification

import com.unifina.utils.testutils.ModuleTestHelper

class StringLengthSpec extends ModuleSpecification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }

	void "StringLength works properly"() {
		module = new StringLength()
		module.init()
		when:
		Map inputValues = [
			text: ["123", "a", "abc", "", "ABCDEFG"]
		]
		Map outputValues = [
			length: [3, 1, 3, 0, 7].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
