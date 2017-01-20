package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class VariadicGetMultiFromMapSpec extends Specification {
	VariadicGetMultiFromMap module

	def setup() {
		module = new VariadicGetMultiFromMap()
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
				[a: false, b: false, c: false, "deep.deep.inside": false],
				[a: true, b: false, c: true, "deep.deep.inside": false],
				[a: false, b: true, c: true, "deep.deep.inside": false],
				[a: true, b: true, c: true, "deep.deep.inside": false],
				[a: false, b: false, c: false, "deep.deep.inside": true]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
