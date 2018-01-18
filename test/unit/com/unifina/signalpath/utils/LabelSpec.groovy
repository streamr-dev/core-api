package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.utils.testutils.ModuleTestHelper

class LabelSpec extends UiChannelMockingSpecification {

	Label module

	def setup() {
		mockServicesForUiChannels()
		module = setupModule(new Label(), [uiChannel: [id: "labelChannel"]])
	}

	void "label sends correct data to uiChannel"() {
		when:
		Map inputValues = [
			label: ["a", "b", "c", "d", "e"],
		]
		Map outputValues = [:]
		Map channelMessages = [
			labelChannel: [
				[value: "a"],
				[value: "b"],
				[value: "c"],
				[value: "d"],
				[value: "e"]
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.test()
	}
}