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
			in1: ["hello", "C", "con"],
			in2: [" world", "++", "catenate"]
		]
		Map outputValues = [
			out: ["hello world", "C++", "concatenate"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "stringConcatenate also works with variadic inputs"() {
		when:
		module.getInput("in3")
		module.getInput("in4")
		module.configure([:])
		Map inputValues = [
				in1: ["hello", "C", "con"],
				in2: [" world", "++", "catenate"],
				in3: ["!", "--", "d"],
				in4: ["", "", ""]
		]
		Map outputValues = [
				out: ["hello world!", "C++--", "concatenated"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
