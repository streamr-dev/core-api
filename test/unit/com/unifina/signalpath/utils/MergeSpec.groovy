package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MergeSpec extends Specification {

	Merge module

	def setup() {
		module = new Merge()
		module.init()
		module.configure([:])
	}

	void "merge gives the right answer"() {
		when:
		Map inputValues = [
			A: [0, 1, null, null, 4, 5, 666],
			B: [0, null, 2, 3, null, null, 999]
		]
		Map outputValues = [
			out: [0, 1, 2, 3, 4, 5, 666]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}