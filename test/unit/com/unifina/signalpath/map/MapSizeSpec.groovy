package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MapSizeSpec extends Specification {
	MapSize module

	def setup() {
		module = new MapSize()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "MapSize works as expected"() {

		Map inputValues = [
		    in: [
		        [:],
				[a: "a", b: 6d],
				Collections.unmodifiableMap([d: "d", e: "e", f: "f", g: "g"])
		    ],
		]

		Map outputValues = [
			size: [0, 2, 4]*.doubleValue()
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
