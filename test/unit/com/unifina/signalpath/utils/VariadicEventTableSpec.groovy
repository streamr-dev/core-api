package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpec
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.SignalPath
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class VariadicEventTableSpec extends UiChannelMockingSpec {

	def final static format = "yyyy-MM-dd HH:mm:ss.SSS";

	VariadicEventTable module

	def setup() {
		mockServicesForUiChannels()

		module = new VariadicEventTable()
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, new SecUser())
		module.parentSignalPath = new SignalPath()
		module.init()
		module.getInput("in1")
		module.getInput("in2")
		module.getInput("in3")
		module.getInput("in4")
		module.configure([uiChannel: [id: "uiChannel"]])
	}

	def cleanup() {
		cleanupMockBeans()
	}

	void "eventTable sends correct data to uiChannel"() {
		when:
		Map inputValues = [
			in1: ["a", "b", "c", "d"],
			in2: [1, 2, 3, 4],
			in3: [null, null, "hello", "world"],
		]
		Map outputValues = [:]
		Map channelMessages = [
			uiChannel: [
				[hdr: [headers: ["timestamp", "outputForin1", "outputForin2", "outputForin3"], title: null]],
				[nr: [new Date(0).format(format), "a", "1", null]],
				[nr: [new Date(60 * 1000).format(format), "b", "2", null]],
				[nr: [new Date(60 * 1000 * 2).format(format), "c", "3", "hello"]],
				[nr: [new Date(60 * 1000 * 3).format(format), "d", "4", "world"]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.timeToFurtherPerIteration(60 * 1000)
			.test()
	}
}