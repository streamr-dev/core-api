package com.unifina.signalpath.bool

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class XorSpec extends Specification {

	Xor module

	def setup() {
		module = new Xor()
		module.init()
		module.configure(module.configuration)
	}

	void "or gives the right answer"() {
		when:
		Map inputValues = [
			A: [false, false, true, true],
			B: [false, true, false, true]
		]
		Map outputValues = [
			out: [false, true, true, false]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
