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
	
	void "words starting with a capital letter"() {
		module.getInput("pattern").receive("([A-Z])\\w+")		
		when:
		Map inputValues = [
			text: ["test", "Secondtest", "thirdTest", "FOURTHTEST", "the Fifth Regex test"].collect {it?.toString()}
		]
		Map outputValues = [
			"match?": [0, 1, 1, 1, 1].collect {it?.doubleValue()},
			"matchCount": [0, 1, 1, 1, 2].collect {it?.doubleValue()},
			"matchList": [[], ['Secondtest'], ['Test'], ['FOURTHTEST'], ['Fifth', 'Regex']]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "phone numbers in the pattern"() {
		module.getInput("pattern").receive("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b")
		when:
		Map inputValues = [
			text: ["p:444-555-1234 + 1235554567", "246.555.8888", "m:1235554567", "not a phone number", "eleven numbers: 01234567890"].collect {it?.toString()}
		]
		Map outputValues = [
			"match?": [1, 1, 1, 0, 0].collect {it?.doubleValue()},
			"matchList": [["444-555-1234", "1235554567"], ['246.555.8888'], ['1235554567'], [], []],
			"matchCount": [2, 1, 1, 0, 0].collect {it?.doubleValue()},
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "ignoreCase off and on"() {
		module.getInput("pattern").receive("foo")
		when:
		Map inputValues = [
			text: ["Foobah", "foo\nfoo", "FOO", "bah"].collect {it?.toString()}
		]
		Map outputValues = [
			"match?": [0, 1, 0, 0].collect {it?.doubleValue()},
			"matchList": [[], ["foo", "foo"], [], []],
			"matchCount": [0,2,0,0].collect {it?.doubleValue()},
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()

		when:
    	module.onConfiguration([options: [ignoreCase: [value: true]]])
		outputValues = [
			"match?": [1, 1, 1, 0].collect {it?.doubleValue()},
			"matchList": [["Foo"], ["foo", "foo"], ["FOO"], []],
			"matchCount": [1,2,1,0].collect {it?.doubleValue()},
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
