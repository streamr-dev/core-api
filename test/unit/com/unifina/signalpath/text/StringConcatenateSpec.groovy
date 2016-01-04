package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class StringConcatenateSpec extends Specification {

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
