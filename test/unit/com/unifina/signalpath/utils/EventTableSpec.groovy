package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.User
import com.unifina.signalpath.SignalPath
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock

import java.text.SimpleDateFormat

@Mock(User)
class EventTableSpec extends UiChannelMockingSpecification {

	SimpleDateFormat dateFormat
	EventTable module

	def setup() {
		mockServicesForUiChannels()
		User user = new User(username: 'user').save(failOnError: true, validate: false)
		module = setupModule(
				new EventTable(),
				[
					uiChannel: [id: "table"],
					options: [inputs: [value: 3]]
				], new SignalPath(true), mockGlobals([:], user))

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
				[nr: [0, [__streamr_date: 0], "a", "1", null]],
				[nr: [new Date(60 * 1000).getTime(), [__streamr_date: 60 * 1000], "b", "2", null]],
				[nr: [new Date(60 * 1000 * 2).getTime(), [__streamr_date: 60 * 1000 * 2], "c", "3", "hello"]],
				[nr: [new Date(60 * 1000 * 3).getTime(), [__streamr_date: 60 * 1000 * 3], "d", "4", "world"]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.timeToFurtherPerIteration(60 * 1000)
			.test()
	}
}
