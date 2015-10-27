package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringReplaceSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }

	void "StringReplace works properly"() {
		module = new StringReplace()
		module.init()
		module.getInput("search").receive("foo")
		module.getInput("replaceWith").receive("bah")
		when:
		Map inputValues = [
			text: ["foobah", "foo", "bah", "abcdeFoog"]
		]
		Map outputValues = [
			"out": ["bahbah", "bah", "bah", "abcdeFoog"]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
