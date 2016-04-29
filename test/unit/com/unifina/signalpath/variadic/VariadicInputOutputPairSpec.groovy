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
		getOutputByDisplayName(module, "out1").value == 666
		getOutputByDisplayName(module, "out2").value == "hello"
		getOutputByDisplayName(module, "out3").value == "world"
	}

	def "getInputValues() returns values of inputs"() {
		getInputByDisplayName(module, "in1").receive("hello")
		getInputByDisplayName(module, "in2").receive("world")
		getInputByDisplayName(module, "in3").receive("!")
		expect:
		inputOutputPairs.inputValues == ["hello", "world", "!"]
	}

	@CompileStatic
	Output getOutputByDisplayName(AbstractSignalPathModule module, String displayName) {
		module.outputs.find { Output output -> output.displayName == displayName }
	}

	@CompileStatic
	Input getInputByDisplayName(AbstractSignalPathModule module, String displayName) {
		module.inputs.find { Input input -> input.displayName == displayName }
	}
}
