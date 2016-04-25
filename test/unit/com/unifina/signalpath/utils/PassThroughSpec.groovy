package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PassThroughSpec extends Specification {

	PassThrough module

	def setup() {
		module = new PassThrough()
		module.init()
		module.configure([
		    options: [
		        inputOutputPairs: [value: 3]
		    ]
		])
	}

	void "passThrough gives the right answer"() {
		when:
		Map inputValues = [
			"in1": (1..10).collect { it?.doubleValue() },
			"in2": (10..19).collect { it?.doubleValue() },
			"in3": (20..29).collect { it?.doubleValue() },
		]
		Map outputValues = [
			"out1": (1..10).collect { it?.doubleValue() },
			"out2": (10..19).collect { it?.doubleValue() },
			"out3": (20..29).collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
