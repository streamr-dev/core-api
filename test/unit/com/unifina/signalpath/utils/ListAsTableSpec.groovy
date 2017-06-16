package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.RuntimeRequest
import com.unifina.signalpath.RuntimeResponse
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.support.GrailsUnitTestMixin

@Mixin(GrailsUnitTestMixin)
class ListAsTableSpec extends UiChannelMockingSpecification {
	ListAsTable module

	RuntimeResponse initResponse = new RuntimeResponse()

	def setup() {
		mockServicesForUiChannels()
		module = setupModule(new ListAsTable(), [
			uiChannel: [id: "table"],
		])
	}

	def "initial headers are set correctly"() {
		when:
		module.handleRequest(new RuntimeRequest([type: "initRequest"], null, null, null, null, new HashSet<>()), initResponse);
		then:
		initResponse.initRequest.hdr.headers == ["List is empty"]
	}

	def "ListAsTable works for value lists"() {
		when:
		Map inputValues = [
		    list: [
		        [],
				[],			// Repetition won't produce a message
				[],
				[5, 4, 3, 21],
				["asdf", "qwer", "rty", "ert"],
				[1, true, "test", ["list", false]],
				[],
				[],
			]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["List is empty"]]],
				[nc: [[]]],
				[hdr: [headers: ["i", "value"]]],
				[nc: [[0, 5], [1, 4], [2, 3], [3, 21]]],
				[nc: [[0, "asdf"], [1, "qwer"], [2, "rty"], [3, "ert"]]],
				[nc: [[0, 1], [1, true], [2, "test"], [3, ["list", false]]]],
				[hdr: [headers: ["List is empty"]]],
				[nc: [[]]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}

	def "ListAsTable works for Map lists"() {
		when:
		Map inputValues = [
			list: [
				[[:]],
				[[q: "A", w: "B"], [q: "C", w: "D"]],
				[[q: "A"], [q: "C", w: "D"], [w: "E"]],
				[[q: "A", w: "B"], [q: "C", r: "D"]],
				[[a: 1, b: true], [a: "test", b: ["list", false]]]
			]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["i"]]],				// looks kind of stupid, but empty objects ARE kind of stupid
				[nc: [[0]]],
				[hdr: [headers: ["i", "q", "w"]]],
				[nc: [[0, "A", "B"], [1, "C", "D"]]],	// same headers, won't be re-sent
				[nc: [[0, "A", ""], [1, "C", "D"], [2, "", "E"]]],
				[hdr: [headers: ["i", "q", "w", "r"]]],
				[nc: [[0, "A", "B", ""], [1, "C", "", "D"]]],
				[hdr: [headers: ["i", "a", "b"]]],
				[nc: [[0, 1, true], [1, "test", ["list", false]]]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}

	def "ListAsTable works for crazy mixed Map/non-Map lists"() {
		when:
		Map inputValues = [
			list: [
				[[], [:]],
				[[:], 2, ""],
				[[q: "A", w: "B"], 1, 2, [1, 2]],
				[[1: true, "e": "V"], ["list"], "test"]
			]
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["i", "value"]]],
				[nc: [[0, []], [1, ""]]],
				[nc: [[0, ""], [1, 2], [2, ""]]],
				[hdr: [headers: ["i", "value", "q", "w"]]],
				[nc: [[0, "", "A", "B"], [1, 1], [2, 2], [3, [1, 2]]]],
				[hdr: [headers: ["i", "value", 1, "e"]]],
				[nc: [[0, "", true, "V"], [1, ["list"]], [2, "test"]]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}
}
