package com.unifina.signalpath.map

import com.unifina.signalpath.Output
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class GetMultiFromMapSpec extends Specification {
	GetMultiFromMap module

	def setup() {
		module = new GetMultiFromMap()
		module.init()
		module.getOutput("endpoint-12312321")
		module.getOutput("endpoint-23213242")
		module.getOutput("endpoint-32423232")
		module.getOutput("endpoint-45545454")
		module.getOutput("endpoint-not-used-placeholder")
		module.configure([:])

		// Rename outputs
		module.getOutput("endpoint-12312321").displayName = "a"
		module.getOutput("endpoint-23213242").displayName = "b"
		module.getOutput("endpoint-32423232").displayName = "c"
		module.getOutput("endpoint-45545454").displayName = "deep.deep.inside"
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
			"a": [null,              666,       666,     1, 1],
			"b": [null,             null, "two-two",     2, 2],
			"c": [null, [hello: "world"],        42,     3, 3],
			"deep.deep.inside": [null,             null,       null, null, "oh, yeah"],
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
