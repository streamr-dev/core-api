package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class TailListSpec extends Specification {
	TailList module

	def setup() {
		module = new TailList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "TailList works as expected"() {

		Map inputValues = [
			limit: [ 2,  null,             null,               10,                0,],
		    in:    [[], ["a"], (1..10).toList(), (1..10).toList(), (1..10).toList(),],
		]

		Map outputValues = [
			out: [[], ["a"], 9..10, 1..10, []],
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
