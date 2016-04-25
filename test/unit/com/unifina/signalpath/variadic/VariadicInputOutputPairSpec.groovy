package com.unifina.signalpath.variadic

import com.unifina.signalpath.simplemath.Multiply
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
		module.getOutput("out1").value == 666
		module.getOutput("out2").value == "hello"
		module.getOutput("out3").value == "world"
	}

	def "getInputValues() returns values of inputs"() {
		module.getInput("in1").receive("hello")
		module.getInput("in2").receive("world")
		module.getInput("in3").receive("!")
		expect:
		inputOutputPairs.inputValues == ["hello", "world", "!"]
	}
}
