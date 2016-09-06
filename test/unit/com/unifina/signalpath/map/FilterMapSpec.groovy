package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class FilterMapSpec extends Specification {
	FilterMap module

	def setup() {
		module = new FilterMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "FilterMap works as expected"() {
		when:
		Map inputValues = [
			keys: [
			    ["a", "c"],
				["b", "d"],
				[],
				["a", "e", "f", "g", "h"],
				null
			],
			in: [
			    [a: 612, b: [d: [e: 550, f: 62] ], c: "cello", d: [truthy: false]],
				[a: 612, b: [d: [e: 550, f: 62] ], c: "cello", d: [truthy: false]],
				[a: 612, b: [d: [e: 550, f: 62] ], c: "cello", d: [truthy: false]],
				[a: 612, b: [d: [e: 550, f: 62] ], c: "cello", d: [truthy: false]],
				[e: "ello", h: [horld: 666]]
			]
		]

		Map outputValues = [
		    out: [
		        [a: 612, c: "cello"],
				[b: [d: [e: 550, f: 62]], d: [truthy: false]],
				[a: 612, b: [d: [e: 550, f: 62] ], c: "cello", d: [truthy: false]],
				[a: 612],
				[e: "ello", h: [horld: 666]]
		    ]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
