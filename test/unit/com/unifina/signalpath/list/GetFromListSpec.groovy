package com.unifina.signalpath.list

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class GetFromListSpec extends Specification {
	GetFromList module

	def setup() {
		module = new GetFromList()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "getFromList returns correct items"() {
		List myList = ["asdf", 1, 2, true, "four"]
		Map inputValues = [
		    index: [0, 4, -1, -5],
			in: [myList] * 4
		]
		Map outputValues = [
		    error: [null] * 4,
			out: ["asdf", "four", "four", "asdf"]
		]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "getFromList returns error on empty or out of bounds"() {
		List myList = ["asdf", 1, 2, true, "four"]
		Map inputValues = [
			index: [0, 5, -6, 0],
			in: [myList, myList, myList, []]
		]
		Map outputValues = [
			error: [null, GetFromList.getOutOfBoundsErrorMessage(5, 5), GetFromList.getOutOfBoundsErrorMessage(-6, 5), GetFromList.emptyError],
			out: ["asdf", "asdf", "asdf", "asdf"]
		]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
