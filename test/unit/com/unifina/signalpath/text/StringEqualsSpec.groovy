package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringEqualsSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }

	void "StringEquals works properly"() {
		module = new StringEquals()
		module.init()
		module.getInput("search").receive("foo")
		when:
    	module.onConfiguration([options: [ignoreCase: [value: true]]])
		Map inputValues = [
			text: ["foobah", "foo", "Foo", "bahFoo"]
		]
		Map outputValues = [
			"equals?": [0, 1, 1, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
		
		when:
    	module.onConfiguration([options: [ignoreCase: [value: false]]])
		outputValues = [
			"equals?": [0, 1, 0, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
