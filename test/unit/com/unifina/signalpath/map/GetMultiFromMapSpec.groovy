package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class GetMultiFromMapSpec extends Specification {
	GetMultiFromMap module

	def setup() {
		module = new GetMultiFromMap()
		module.init()
		module.configure([
			options: [numOfKeys: [value: 4]],
			outputs: [
				[name: "out-1", displayName: "a"],
				[name: "out-2", displayName: "b"],
				[name: "out-3", displayName: "c"],
				[name: "out-4", displayName: "deep.deep.inside"]
			]
		])
	}

	def "GetMultiFromMap works as expected"() {
		Map inputValues = [
			in: [
				[:],
				[a: 666, c: [hello: "world"]],
				[b: "two-two", c: 42],
				[a: 1, b: 2, c:3],
				[deep: [deep: [inside: "oh, yeah"]]]
			]
		]

		Map outputValues = [
			"out-1": [null,              666,       666,     1, 1],
			"out-2": [null,             null, "two-two",     2, 2],
			"out-3": [null, [hello: "world"],        42,     3, 3],
			"out-4": [null,             null,       null, null, "oh, yeah"],
			founds: [
				[a: 0d, b: 0d, c: 0d, "deep.deep.inside": 0d],
				[a: 1d, b: 0d, c: 1d, "deep.deep.inside": 0d],
				[a: 0d, b: 1d, c: 1d, "deep.deep.inside": 0d],
				[a: 1d, b: 1d, c: 1d, "deep.deep.inside": 0d],
				[a: 0d, b: 0d, c: 0d, "deep.deep.inside": 1d]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
