package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class IndexesOfItemSpec extends Specification {
	IndexesOfItem module

	def setup() {
		module = new IndexesOfItem()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "IndexesOfItem works as expected"() {

		Map inputValues = [
			list: [[], [1, 2, 3], [1, 2, 666, 3], [666, 1, 666, 2, 666]],
			item: [666,      666,            666,                   666]
		]

		Map outputValues = [
			indexes: [[], [], [2d], [0d, 2d, 4d]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
