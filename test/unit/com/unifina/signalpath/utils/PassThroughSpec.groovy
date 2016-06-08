package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class PassThroughSpec extends Specification {

	PassThrough module

	def setup() {
		module = new PassThrough()
		module.init()
	}

	void "passThrough gives the right answer"() {
		when:
		Map inputValues = [
			in: (1..100).collect { it?.doubleValue() },
		]
		Map outputValues = [
			out : (1..100).collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
