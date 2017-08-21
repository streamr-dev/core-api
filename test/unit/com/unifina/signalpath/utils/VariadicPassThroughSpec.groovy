package com.unifina.signalpath.utils

import com.unifina.ModuleTestingSpecification
import com.unifina.utils.testutils.ModuleTestHelper

class VariadicPassThroughSpec extends ModuleTestingSpecification {

	VariadicPassThrough module

	def setup() {
		module = new VariadicPassThrough()
		// Ensure variadic endpoints before configuring module
		module.getInput("input-a")
		module.getInput("input-b")
		module.getInput("input-c")
		module.getInput("input-d")
		module.getOutput("output-a")
		module.getOutput("output-b")
		module.getOutput("output-c")
		module.getOutput("output-d")
		setupModule(module)
	}

	void "passThrough gives the right answer"() {
		when:
		Map inputValues = [
			in1: (1..10).collect { it?.doubleValue() },
			in2: (10..19).collect { it?.doubleValue() },
			in3: (20..29).collect { it?.doubleValue() },
		]
		Map outputValues = [
			out1: (1..10).collect { it?.doubleValue() },
			out2: (10..19).collect { it?.doubleValue() },
			out3: (20..29).collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
