package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.User
import com.unifina.signalpath.SignalPath
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

@TestMixin(GrailsUnitTestMixin)
@Mock(User)
class VariadicEventTableSpec extends UiChannelMockingSpecification {

	SimpleDateFormat dateFormat
	VariadicEventTable module

	def setup() {
		mockServicesForUiChannels()
		User user = new User(username: 'user').save(failOnError: true, validate: false)
		module = setupModule(new VariadicEventTable(), [uiChannel: [id: "uiChannel"]], new SignalPath(true), mockGlobals([:], user))

		// Call getInput to make sure the inputs exist
		module.getInput("in1")
		module.getInput("in2")
		module.getInput("in3")

		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
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
				[nr: [[__streamr_date: 0], "a", "1", null]],
				[nr: [[__streamr_date: 60 * 1000], "b", "2", null]],
				[nr: [[__streamr_date: 60 * 1000 * 2], "c", "3", "hello"]],
				[nr: [[__streamr_date: 60 * 1000 * 3], "d", "4", "world"]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.timeToFurtherPerIteration(60 * 1000)
			.test()
	}
}
