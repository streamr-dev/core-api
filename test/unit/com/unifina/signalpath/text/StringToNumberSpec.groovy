package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class StringToNumberSpec extends Specification {
	StringToNumber module

	def setup() {
		module = new StringToNumber()
		module.init()
		module.configure(module.configuration)
	}

	def "converts correctly"() {
		def inputs = [in: ["1", "-0.5", "-1.e3", "-.1e3", "+1e1", "+.e2", ""]]
		def outputs = [
			out: [1, -0.5, -1000, -100, 10, 10, 10].collect {it?.doubleValue()},
			error: [null, null, null, null, null, "Failed to parse: '+.e2'", "Failed to parse: ''"]
		]
		expect:
		new ModuleTestHelper.Builder(module, inputs, outputs).test()
	}
}
