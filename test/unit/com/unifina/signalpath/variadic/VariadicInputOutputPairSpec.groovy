package com.unifina.signalpath.variadic

import com.unifina.signalpath.simplemath.Multiply
import spock.lang.Specification

class VariadicInputOutputPairSpec extends Specification {
	def module = new Multiply()
	def inputOutputPairs = new VariadicInputOutputPair<Object>(module,
		new InputInstantiator.SimpleObject(), new OutputInstantiator.SimpleObject())

	def setup() {
		inputOutputPairs.addInput("sisääntulo-1")
		inputOutputPairs.addInput("sisääntulo-2")
		inputOutputPairs.addInput("sisääntulo-3")
		inputOutputPairs.addInput("sisääntulo-4")

		inputOutputPairs.addOutput("ulostulo-1")
		inputOutputPairs.addOutput("ulostulo-2")
		inputOutputPairs.addOutput("ulostulo-3")
		inputOutputPairs.addOutput("ulostulo-4")

		inputOutputPairs.onConfiguration(null)
	}

	def "adds pairing details to endpoints' variadic configs"() {
		expect:
		module.inputs*.getConfiguration()*.variadic == [
			[isLast: false, index: 1, linkedOutput: "ulostulo-1"],
			[isLast: false, index: 2, linkedOutput: "ulostulo-2"],
			[isLast: false, index: 3, linkedOutput: "ulostulo-3"],
			[isLast: true,  index: 4, linkedOutput: "ulostulo-4"]
		]

		and:
		module.outputs*.getConfiguration()*.variadic == [
			[isLast: false, disableGrow: true, index: 1],
			[isLast: false, disableGrow: true, index: 2],
			[isLast: false, disableGrow: true, index: 3],
			[isLast: true,  disableGrow: true, index: 4]
		]
	}

	def "can send values to outputs"() {
		when:
		inputOutputPairs.sendValuesToOutputs([666, "hello", "world"])
		then:
		module.outputs*.value == [666, "hello", "world", null] // last element is placeholder
	}

	def "can read values from from inputs"() {
		module.inputs[0].receive(666)
		module.inputs[1].receive("hello")
		module.inputs[2].receive("world")

		when:
		List values = inputOutputPairs.getInputValues()
		then:
		values == [666, "hello", "world"]
	}
}
