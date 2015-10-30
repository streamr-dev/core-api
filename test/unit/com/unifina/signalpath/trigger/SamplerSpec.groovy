package com.unifina.signalpath.trigger

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SamplerSpec extends Specification {

	Sampler module

	def setup() {
		module = new Sampler()
		module.init()
		module.configure([:])
	}

	void "sampler gives the right answer"() {
		when:
		Map inputValues = [
			trigger: [true, null, null, true, true, null, true, null, null],
			value: ["a", "b", "c", "d", "e", "f", "g", "h", "i"],
		]
		Map outputValues = [
			value: ["a", "a", "a", "d", "e", "e", "g", "g", "g"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
