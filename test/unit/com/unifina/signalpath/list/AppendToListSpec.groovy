package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class AppendToListSpec extends Specification {
	AppendToList module

	def setup() {
		module = new AppendToList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "AppendToList works as expected"() {

		Map inputValues = [
		    item: [ 6,   null,    9, [3, 4]],
			list: [[], [6, 6], null, [1, 2]]
		]

		Map outputValues = [
			list: [[6], [6, 6, 6], [6, 6, 9], [1, 2, [3, 4]]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
