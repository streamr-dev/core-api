package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class StringTemplateTest extends Specification {

	StringTemplate module

	void setup() {
		module = new StringTemplate()
		module.init()
	}

	void "Map entries are injected into ST"() {
		when:
		module.configure([
			params : [
				[name: "template", value: "<first> <second> <third>"]
			]
		])
		Map inputValues = [
			args: [[first: 111, second: 22, third: 3], [first: 3, second: 4, third: 5], [:], [first: 1, second: 22, third: 333]]
		]
		Map outputValues = [
			"errors": [[], [], ["context [anonymous] 1:1 attribute first isn't defined", "context [anonymous] 1:9 attribute second isn't defined", "context [anonymous] 1:18 attribute third isn't defined"], []],
			"result": ["111 22 3", "3 4 5", "  ", "1 22 333"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "Missing entries are reported"() {
		when:
		module.configure([
			params : [
				[name: "template", value: "<first> <second> <third>"]
			]
		])
		Map inputValues = [
			args: [[:], [first: 111, second: 22, third: 3], [:]]
		]
		Map outputValues = [
			"errors": [["context [anonymous] 1:1 attribute first isn't defined", "context [anonymous] 1:9 attribute second isn't defined", "context [anonymous] 1:18 attribute third isn't defined"], [], ["context [anonymous] 1:1 attribute first isn't defined", "context [anonymous] 1:9 attribute second isn't defined", "context [anonymous] 1:18 attribute third isn't defined"]],
			"result": ["  ", "111 22 3", "  "]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	/*
	void "Error is reported for malformed template"() {
		when:
		module.configure([
			params : [
				[name: "template", value: "<arg + 1>"]
			]
		])
		Map inputValues = [
			args: [[arg: [x: 3]]]
		]
		Map outputValues = [
			"errors": [["1:5: invalid character '+'", "1:7: '1' came as a complete surprise to me"]],
			"result": [null]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
	*/

	void "Dot notation works"() {
		when:
		module.configure([
			params : [
				[name: "template", value: "<arg.x>"]
			]
		])
		Map inputValues = [
			args: [[arg: [x: 3]], [arg: [x: 4]], [arg: [x: 5]]]
		]
		Map outputValues = [
			"errors": [[], [], []],
			"result": ["3", "4", "5"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
