package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class StringContainsSpec extends Specification {

	StringContains module

	def setup(){
		module = new StringContains()
		module.init()
	}

	void "stringContains with ignoreCase gives the right answer"() {
		when:
		Map inputValues = [
			search: ["hello", "++", "are you doing", "wow", "!"],
			string: ["HELLo wORLD", "C++", "how are you doing, mate?", "kow vow", "..,,."],

		]
		Map outputValues = [
			contains: [1, 1, 1, 0, 0].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "stringContains without ignoreCase gives the right answer"() {
		when:
		module.configure([options: [ignoreCase: [value: false]]])
		Map inputValues = [
			search: ["hello", "++", "are you doing", "wow", "!"],
			string: ["HELLo wORLD", "C++", "how are you doing, mate?", "kow vow", "..,,."],

		]
		Map outputValues = [
			contains: [0, 1, 1, 0, 0].collect {it?.doubleValue()}
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
