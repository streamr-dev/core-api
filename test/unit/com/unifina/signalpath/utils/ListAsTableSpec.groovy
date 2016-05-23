package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ListAsTableSpec extends Specification {
	ListAsTable module

	def setup() {
		module = new ListAsTable()
		module.init()
		module.configure([
			uiChannel: [id: "table"],
		])
	}

	def "ListAsTable works for value lists"() {
		when:
		Map inputValues = [
		    list: [
		        [],
				[5, 4, 3, 21],
				["asdf", "qwer", "rty", "ert"],
				[1, true, "test", ["list", false]]
		    ]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["List is empty"]]],
				[nc: [[]]],
				[hdr: [headers: ["i", "value"]]],
				[nc: [[0, 5], [1, 4], [2, 3], [3, 21]]],
				[hdr: [headers: ["i", "value"]]],
				[nc: [[0, "asdf"], [1, "qwer"], [2, "rty"], [3, "ert"]]],
				[hdr: [headers: ["i", "value"]]],
				[nc: [[0, 1], [1, true], [2, "test"], [3, ["list", false]]]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.test()
	}

	def "ListAsTable works for Map lists"() {
		when:
		Map inputValues = [
			list: [
				[[:]],
				[[q: "A", w: "B"], [q: "C", w: "D"]],
				[[q: "A", w: "B"], [q: "C", r: "D"]],
				[[a: 1, b: true], [a: "test", b: ["list", false]]]
			]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["i"]]],
				[nc: [[0]]],
				[hdr: [headers: ["i", "q", "w"]]],
				[nc: [[0, "A", "B"], [1, "C", "D"]]],
				[hdr: [headers: ["i", "q", "w", "r"]]],
				[nc: [[0, "A", "B", ""], [1, "C", "", "D"]]],
				[hdr: [headers: ["i", "a", "b"]]],
				[nc: [[0, 1, true], [1, "test", ["list", false]]]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.test()
	}

	def "ListAsTable works for crazy mixed Map/non-Map lists"() {
		when:
		Map inputValues = [
			list: [
				[[], [:]],
				[[q: "A", w: "B"], 1, 2, [1, 2]],
				[[1: true, "e": "V"], ["list"], "test"]
			]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["i", "value"]]],
				[nc: [[0, []], [1, ""]]],
				[hdr: [headers: ["i", "value", "q", "w"]]],
				[nc: [[0, "", "A", "B"], [1, 1], [2, 2], [3, [1, 2]]]],
				[hdr: [headers: ["i", "value", 1, "e"]]],
				[nc: [[0, "", true, "V"], [1, ["list"]], [2, "test"]]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages)
			.test()
	}
}
