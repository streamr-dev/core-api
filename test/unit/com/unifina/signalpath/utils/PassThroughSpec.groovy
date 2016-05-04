package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PassThroughSpec extends Specification {

	PassThrough module

	def setup() {
		module = new PassThrough()
		module.init()
		module.configure([
			inputNames: ["endpoint-a", "endpoint-b"],
			outputNames: ["endpoint-d", "endpoint-e"],
		])
	}

	void "passThrough gives the right answer"() {
		when:
		Map inputValues = [
			in: (1..10).collect { it?.doubleValue() },
			in2: (10..19).collect { it?.doubleValue() },
			in3: (20..29).collect { it?.doubleValue() },
		]
		Map outputValues = [
			out: (1..10).collect { it?.doubleValue() },
			out2: (10..19).collect { it?.doubleValue() },
			out3: (20..29).collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
