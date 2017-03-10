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

	def "converts correctly (non-strict)"() {
		module.configure([
			options: [strict: [value: false]]
		])
		def inputs = [in: ["1+1", "3s", "43 ms", "2,4", "+.e2", "+.e", ".", "asdf+3.e2wer", "", "1", "-0.5", "-1.e3", "-.1e3", "+1e1"]]
		def outputs = [out: [1, 3, 43, 2, 2, 0, 0, 300, 0, 1, -0.5, -1000, -100, 10].collect {it?.doubleValue()}]
		expect:
		new ModuleTestHelper.Builder(module, inputs, outputs).test()
	}

	def "converts correctly (strict)"() {
		module.configure([
			options: [strict: [value: true]]
		])
		def inputs = [in: ["1+1", "3s", "43 ms", "2,4", "+.e2", "+.e", ".", "asdf+3.e2wer", "", "1", "-0.5", "-1.e3", "-.1e3", "+1e1"]]
		def outputs = [out: [null, null, null, null, null, null, null, null, null, 1, -0.5, -1000, -100, 10].collect {it?.doubleValue()}]
		expect:
		new ModuleTestHelper.Builder(module, inputs, outputs).test()
	}
}
