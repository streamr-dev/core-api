package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ConstantStringSpec extends Specification {

	ConstantString module

	def setup(){
		module = new ConstantString()
		module.init()
	}

	void "constantString gives the right answer"() {
		when:
		module.getInput("str").receive("hello")

		Map inputValues = [:]
		Map outputValues = [
			out: ["hello", "hello", "hello"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.extraIterationsAfterInput(3)
			.test()
	}
}
