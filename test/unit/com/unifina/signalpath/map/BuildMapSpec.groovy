package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class BuildMapSpec extends Specification {
	BuildMap module

	def setup() {
		module = new BuildMap()
		module.init()
		module.configure([
			options: [numOfKeys: [value: 3]],
			inputs: [
				[name: "in-1", displayName: "a"],
				[name: "in-2", displayName: "b"],
				[name: "in-3", displayName: "c"]
			]
		])
	}

	def "BuildMap works as expected"() {
		Map inputValues = [
			"a": ["hello", 6,    0],
			"b": [    ",", 6, null],
			"c": ["world", 6,    0],
		]

		Map outputValues = [
			map: [
				[a: "hello", b: ",", c: "world"],
				[a: 6, b: 6, c: 6],
				[a: 0, b: 6, c: 0]
			],
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
