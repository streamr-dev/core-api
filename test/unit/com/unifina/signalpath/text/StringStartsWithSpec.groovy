package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringStartsWithSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }

	void "StringStartsWith works properly"() {
		module = new StringStartsWith()
		module.init()
		module.getInput("search").receive("foo")
		when:
    	module.onConfiguration([options: [ignoreCase: [value: true]]])
		Map inputValues = [
			text: ["foobah", "bahfoo", "foo", "bah", "Foobah"]
		]
		Map outputValues = [
			"startsWith?": [1, 0, 1, 0, 1].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
		
		when:
    	module.onConfiguration([options: [ignoreCase: [value: false]]])
		outputValues = [
			"startsWith?": [1, 0, 1, 0, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
