package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Input
import com.unifina.signalpath.Output
import com.unifina.signalpath.simplemath.Multiply
import groovy.transform.CompileStatic
import spock.lang.Specification

class VariadicInputOutputPairSpec extends Specification {
	def module = new Multiply()
	def inputOutputPairs = new VariadicInputOutputPair<Object>(module, 3,
		new InputInstantiator.SimpleObject(), new OutputInstantiator.SimpleObject())

	def setup() {
		inputOutputPairs.onConfiguration([:])
	}

	def "getConfiguration() provides expected module options and configurations"() {
		def config = [:]

		when:
		inputOutputPairs.getConfiguration(config)

		then:
		config == [
			variadicInput: true,
			variadicOutput: true,
			options: [
				inputOutputPairs: [value: 3, type: "int"]
			]
		]
	}

	def "sendValuesToOutputs() sends values to outputs"() {
		when:
		inputOutputPairs.sendValuesToOutputs([666, "hello", "world"])
		then:
		module.findOutputByDisplayName("out1").value == 666
		module.findOutputByDisplayName("out2").value == "hello"
		module.findOutputByDisplayName("out3").value == "world"
	}

	def "getInputValues() returns values of inputs"() {
		module.findInputByDisplayName("in1").receive("hello")
		module.findInputByDisplayName("in2").receive("world")
		module.findInputByDisplayName("in3").receive("!")
		expect:
		inputOutputPairs.inputValues == ["hello", "world", "!"]
	}
}
