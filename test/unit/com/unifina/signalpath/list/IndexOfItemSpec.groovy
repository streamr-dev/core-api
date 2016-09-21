package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class IndexOfItemSpec extends Specification {
	IndexOfItem module

	def setup() {
		module = new IndexOfItem()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "IndexOfItem works as expected"() {

		Map inputValues = [
			list: [[], [1, 2, 3], [1, 2, 666, 3], [666, 1, 666, 2, 666]],
			item: [666,      666,            666,                   666]
		]

		Map outputValues = [
			index: [null, null, 2d, 0d]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
