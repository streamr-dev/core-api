package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class TailMapSpec extends Specification {
	TailMap module

	def setup() {
		module = new TailMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "tailMap() works on Map as UNIX `tail`"() {
		module.getInput("limit").receive(3)

		Map inputValues = [
		    in: [
		        [:] as LinkedHashMap,
				[a: "hello", b: "world", c: "!"] as LinkedHashMap,
				[a: "large", b: "map", c: 666, d: "d", e: "e", f: "", g: "g"] as LinkedHashMap,
		    ]
		]

		Map outputValues = [
			out: [
				[:],
				[a: "hello", b: "world", c: "!"],
				[e: "e", f: "", g: "g"]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	def "tailMap() works on NavigableMap as UNIX `tail`"() {
		module.getInput("limit").receive(3)

		Map inputValues = [
			in: [
				[:] as TreeMap,
				[a: "hello", b: "world", c: "!"] as TreeMap,
				[a: "large", b: "map", c: 666, d: "d", e: "e", f: "", g: "g"] as TreeMap,
			]
		]

		Map outputValues = [
			out: [
				[:],
				[a: "hello", b: "world", c: "!"],
				[e: "e", f: "", g: "g"]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
