package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class AddToListSpec extends Specification {
	AddToList module

	def setup() {
		module = new AddToList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "AddToList error cases works as expected"() {
		Map inputValues = [
			index: [1, -1],
			item:  [6, 6],
			list:  [[], [1, 2, 3]]
		]

		Map outputValues = [
			error: ["Index: 1, Size: 0", "Index: -1, Size: 3"],
			list:  [null, null]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "AddToList works as expected"() {

		Map inputValues = [
			index: [0,       null,         2,    3],
		    item:  [6,       null,      null,    0],
			list:  [[], [1, 2, 3], [1, 1, 1], null]
		]

		Map outputValues = [
			error: [null, null, null, null],
			list:  [[6], [6, 1, 2, 3], [1, 1, 6, 1], [1, 1, 1, 0]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
