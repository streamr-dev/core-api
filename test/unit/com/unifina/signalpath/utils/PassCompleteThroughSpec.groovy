package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PassCompleteThroughSpec extends Specification {

	PassCompleteThrough module

	def setup() {
		module = new PassCompleteThrough()
		module.getInput("input-a")
		module.getInput("input-b")
		module.getInput("input-c")
		module.getInput("input-d")
		module.getOutput("output-a")
		module.getOutput("output-b")
		module.getOutput("output-c")
		module.getOutput("output-d")
		module.init()
		module.configure(module.configuration)
	}

	void "passThrough gives the right answer"() {
		when:
		Map inputValues = [
			in1: [null, 1,          3, 5,    8, null, 10],
			in2: [null, 2,          4, 6, null, null, 11],
			in3: [null, null, "hello", 7,    9, null, 12],
		]
		Map outputValues = [
			out1: [null, null, 3,       5, 5, 5, 10],
			out2: [null, null, 4,       6, 6, 6, 11],
			out3: [null, null, "hello", 7, 7, 7, 12],
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
