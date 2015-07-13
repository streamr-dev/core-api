package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper

class StringModulesSpec extends Specification {
	
	def module

	def setup(){
		module = null
	}

    def cleanup() {
		
    }

    def doubleTest(module, inputValues, outputValues, ignoreCase = "true") {
    	
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
    	module.onConfiguration([options: [ignoreCase: [value: false]]])
		outputValues = [
			"endsWith?": [0, 1, 1, 0, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
    	module.onConfiguration([options: [ignoreCase: [value: false]]])
		outputValues = [
			"equals?": [0, 1, 0, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
    	module.onConfiguration([options: [ignoreCase: [value: false]]])
		outputValues = [
			"startsWith?": [1, 0, 1, 0, 0].collect {it?.doubleValue()}
		]
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
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
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}

	void "ValueAsText works properly"() {
		module = new ValueAsText()
		module.init()
		when:
		int integer = 1234
		ArrayList<Integer> list = new ArrayList<Integer>()
		list.add(1)
		list.add(2)
		list.add(3)
		HashMap<Integer, Map<String, ArrayList<String>>> map = new HashMap<Integer, Map<String, ArrayList<String>>>()
		map.put(0, new HashMap<String, ArrayList<String>>())
		map.get(0).put("a", new ArrayList<String>())
		map.get(0).get("a").add("a")
		map.get(0).get("a").add("b")

		Map inputValues = [
			in: [integer, list, map, "string"]
		]
		Map outputValues = [
			text: ["1234", "[1, 2, 3]", "{0={a=[a, b]}}" , "string"]
		]
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
		
		when:
		module.clearState()
		
		then:
		new ModuleTestHelper(module, inputValues, outputValues).test()
	}
}
