package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class StringConcatenateSpec extends ModuleSpecification {

	StringConcatenate module

	def setup(){
		module = new StringConcatenate()
		module.init()
	}

	void "stringConcatenate gives the right answer"() {
		when:
		Map inputValues = [
			A: ["hello", "C", "con"],
			B: [" world", "++", "catenate"],

		]
		Map outputValues = [
			AB: ["hello world", "C++", "concatenate"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
