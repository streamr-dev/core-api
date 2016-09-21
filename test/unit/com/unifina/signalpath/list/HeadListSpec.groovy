package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class HeadListSpec extends Specification {
	HeadList module

	def setup() {
		module = new HeadList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "HeadList works as expected"() {

		Map inputValues = [
			limit: [ 2,  null,             null,               10,                0,],
		    in:    [[], ["a"], (1..10).toList(), (1..10).toList(), (1..10).toList(),],
		]

		Map outputValues = [
			out: [[], ["a"], 1..2, 1..10, []],
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
