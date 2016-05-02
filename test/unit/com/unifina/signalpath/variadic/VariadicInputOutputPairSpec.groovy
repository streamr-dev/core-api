package com.unifina.signalpath.variadic

import com.unifina.signalpath.simplemath.Multiply
import spock.lang.Specification

class VariadicInputOutputPairSpec extends Specification {
	def module = new Multiply()
	def inputOutputPairs = new VariadicInputOutputPair<Object>(module,
		new InputInstantiator.SimpleObject(), new OutputInstantiator.SimpleObject(), 3)

	def setup() {
		inputOutputPairs.onConfiguration([:])
	}

	def "getConfiguration() provides expected module options and configurations"() {
		def config = [:]

		when:
		inputOutputPairs.getConfiguration(config)

		then:
		config.keySet() == ["options", "inputNames", "outputNames", "variadicInput", "variadicOutput"] as Set

		and:
		config.options == [
			inputOutputPairs: [value: 3, type: "int"],
		]

		and:
		config.inputNames.size() == 3
		config.outputNames.size() == 3

		and:
		config.variadicInput == true
		config.variadicOutput == true
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
