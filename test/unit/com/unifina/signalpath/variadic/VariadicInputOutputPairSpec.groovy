package com.unifina.signalpath.variadic

import com.unifina.signalpath.simplemath.Multiply
import spock.lang.Specification

class VariadicInputOutputPairSpec extends Specification {
	def module = new Multiply()
	def inputOutputPairs = new VariadicInputOutputPair<Object>(module,
		new InputInstantiator.SimpleObject(), new OutputInstantiator.SimpleObject())

	def setup() {
		inputOutputPairs.onConfiguration([
			inputNames: ["endpoint-a", "endpoint-b"],
			outputNames: ["endpoint-c", "endpoint-d"]
		])
	}

	def "getConfiguration() provides expected module options and configurations"() {
		def config = [:]

		when:
		inputOutputPairs.getConfiguration(config)

		then:
		config.keySet() == ["inputNames", "outputNames", "variadicInput", "variadicOutput"] as Set

		and:
		config.inputNames.size() == 2
		config.outputNames.size() == 2

		and:
		config.variadicInput == true
		config.variadicOutput == true
	}

	def "sendValuesToOutputs() sends values to outputs"() {
		when:
		inputOutputPairs.sendValuesToOutputs([666, "hello world"])
		then:
		module.findOutputByDisplayName("out1").value == 666
		module.findOutputByDisplayName("out2").value == "hello world"
	}

	def "getInputValues() returns values of inputs"() {
		module.findInputByDisplayName("in1").receive("hello")
		module.findInputByDisplayName("in2").receive("world")
		expect:
		inputOutputPairs.inputValues == ["hello", "world"]
	}
}
