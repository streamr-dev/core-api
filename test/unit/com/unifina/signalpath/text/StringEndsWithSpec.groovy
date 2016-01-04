package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringEndsWithSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }
	
	void "StringEndsWith works properly"() {
		module = new StringEndsWith()
		module.init()
		module.getInput("search").receive("foo")
		when:
    	module.onConfiguration([options: [ignoreCase: [value: true]]])
		Map inputValues = [
			text: ["foobah", "bahfoo", "foo", "bah", "bahFoo"]
		]
		Map outputValues = [
			"endsWith?": [0, 1, 1, 0, 1].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
		
		when:
    	module.onConfiguration([options: [ignoreCase: [value: false]]])
		outputValues = [
			"endsWith?": [0, 1, 1, 0, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
