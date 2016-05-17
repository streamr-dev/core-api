package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PassThroughSpec extends Specification {

	PassThrough module

	def setup() {
		module = new PassThrough()
		module.getInput("input-a")
		module.getInput("input-b")
		module.getInput("input-c")
		module.getInput("input-d")
		module.getOutput("output-a")
		module.getOutput("output-b")
		module.getOutput("output-c")
		module.getOutput("output-d")
		module.init()
		module.configure([:])
	}

	void "passThrough gives the right answer"() {
		when:
		Map inputValues = [
			"input-a": (1..10).collect { it?.doubleValue() },
			"input-b": (10..19).collect { it?.doubleValue() },
			"input-c": (20..29).collect { it?.doubleValue() },
		]
		Map outputValues = [
			"output-a": (1..10).collect { it?.doubleValue() },
			"output-b": (10..19).collect { it?.doubleValue() },
			"output-c": (20..29).collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
