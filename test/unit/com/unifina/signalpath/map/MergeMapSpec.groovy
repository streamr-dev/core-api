package com.unifina.signalpath.map

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class MergeMapSpec extends Specification {
	MergeMap module

	def setup() {
		module = new MergeMap()
		module.init()
		module.configure(module.getConfiguration())
	}

	def "MergeMap() works as expected"() {

		Map inputValues = [
		    leftMap: [
		        [:],
				[:],
				[c: "c", d: "d"],
				[e: "e", f: "f"],
				[i: "i", j: "j", conflict: 666],
				Collections.unmodifiableMap([o: "o", p: "p"])
		    ],
			rightMap: [
				[:],
				[a: "a", b: "b"],
				[:],
				[g: "g", h: "h"],
				[conflict: "999", l: "l", m: "m"],
				[q: "q", r: "r"]
			],
		]

		Map outputValues = [
			out: [
			    [:],
				[a: "a", b: "b"],
				[c: "c", d: "d"],
				[e: "e", f: "f", g: "g", h: "h"],
				[i: "i", j: "j", conflict: "999", l: "l", m: "m"],
				[o: "o", p: "p", q: "q", r: "r"]
			]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
