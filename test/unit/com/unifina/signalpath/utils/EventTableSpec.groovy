package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.SignalPath
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

@Mixin(GrailsUnitTestMixin)
class EventTableSpec extends UiChannelMockingSpecification {

	SimpleDateFormat dateFormat
	EventTable module

	def setup() {
		mockServicesForUiChannels()
		module = setupModule(
				new EventTable(),
				[
					uiChannel: [id: "table"],
					options: [inputs: [value: 3]]
				])

		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
	}

	void "eventTable sends correct data to uiChannel"() {
		when:
		Map inputValues = [
			input1: ["a", "b", "c", "d"],
			input2: [1, 2, 3, 4],
			input3: [null, null, "hello", "world"],
		]
		Map outputValues = [:]
		Map channelMessages = [
			table: [
				[hdr: [headers: ["timestamp", "outputForinput1", "outputForinput2", "outputForinput3"]]],
				[nr: [dateFormat.format(new Date(0)), "a", "1", null]],
				[nr: [dateFormat.format(new Date(60 * 1000)), "b", "2", null]],
				[nr: [dateFormat.format(new Date(60 * 1000 * 2)), "c", "3", "hello"]],
				[nr: [dateFormat.format(new Date(60 * 1000 * 3)), "d", "4", "world"]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.timeToFurtherPerIteration(60 * 1000)
			.test()
	}
}