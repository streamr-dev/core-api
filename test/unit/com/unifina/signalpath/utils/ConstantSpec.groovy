package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class ConstantSpec extends ModuleSpecification {

	Constant module

	def setup() {
		module = new Constant()
		module.init()
	}

	void "constant gives the right answer"() {
		when:
		Map inputValues = [
			constant: [1, 2, 3, 4, 5].collect { it?.doubleValue() },
		]
		Map outputValues = [
			out : [1, 2, 3, 4, 5].collect { it?.doubleValue() },
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
