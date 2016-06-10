package com.unifina.signalpath.utils

import com.unifina.utils.testutils.ModuleTestHelper
import com.unifina.signalpath.ModuleSpecification

class LabelSpec extends ModuleSpecification {

	Label module

	def setup() {
		module = new Label()
		module.init()
		module.configure([uiChannel: [id: "labelChannel"]])
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
			.uiChannelMessages(channelMessages)
			.test()
	}
}