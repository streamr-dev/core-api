package com.unifina.signalpath.trigger

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class SamplerConditionalSpec extends Specification {

	SamplerConditional module

	def setup() {
		module = new SamplerConditional()
		module.init()
		module.configure([:])
	}

	void "samplerConditional gives the right answer"() {
		when:
		Map inputValues = [
			triggerIf: [1, null, 0, 1, 1, 0, 1, 0, null].collect { it?.doubleValue() },
			value: ["a", "b", "c", "d", "e", "f", "g", "h", "i"],
		]
		Map outputValues = [
			value: ["a", "a", "a", "d", "e", "e", "g", "g", "g"]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
