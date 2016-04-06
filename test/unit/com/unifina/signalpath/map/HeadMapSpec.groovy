package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class HeadMapSpec extends Specification {
	HeadMap module

	def setup() {
		module = new HeadMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "headMap() works as UNIX `head`"() {
		module.getInput("limit").receive(3)

		Map inputValues = [
		    in: [
		        [:] as LinkedHashMap,
				[a: "hello", b: "world", c: "!"] as LinkedHashMap,
				[a: "large", b: "map", c: 666, d: "d", e: "e", f: "", g: "g"]
		    ]
		]

		Map outputValues = [
			out: [
				[:],
				[a: "hello", b: "world", c: "!"],
				[a: "large", b: "map", c: 666]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
