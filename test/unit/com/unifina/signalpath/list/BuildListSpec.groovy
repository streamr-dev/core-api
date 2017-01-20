package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class BuildListSpec extends Specification {
	BuildList module

	def setup() {
		module = new BuildList()
		module.init()
		(0..4).each { module.getInput("in$it") }
		module.configure(module.getConfiguration())
	}

	def "BuildList works as expected"() {
		Map inputValues = [
		    in0: [1, null],
			in1: [6, 3],
			in2: [9, 3],
			in3: [5, 7]
		]

		Map outputValues = [
			out: [[1, 6, 9, 5], [1, 3, 3, 7]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
