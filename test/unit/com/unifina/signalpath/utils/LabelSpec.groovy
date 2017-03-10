package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpec
import com.unifina.domain.security.SecUser
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.support.GrailsUnitTestMixin

@Mixin(GrailsUnitTestMixin)
class LabelSpec extends UiChannelMockingSpec {

	Label module

	def setup() {
		mockServicesForUiChannels()
		module = new Label()
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, new SecUser())
		module.init()
		module.configure([uiChannel: [id: "labelChannel"]])
	}

	def cleanup() {
		cleanupMockBeans()
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