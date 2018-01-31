package com.unifina.signalpath.time

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

@Mock(SecUser)
class SchedulerSpec extends UiChannelMockingSpecification {
	Scheduler module
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
	TimeZone UTC = TimeZone.getTimeZone("UTC")

	def setup() {
		mockServicesForUiChannels()
		TimeZone Helsinki = TimeZone.getTimeZone("Europe/Helsinki")

		df.setTimeZone(Helsinki)
	}

	void "Scheduler works as expected"() {
		when:
		module = setupModule(new Scheduler(), [
			uiChannel: [id: "schedulerChannel"],
			schedule: [
				defaultValue: 100,
				rules: [
					[value: 10, intervalType: 1, startDate:[hour:0, minute:30], endDate:[hour: 8, minute:30]],  // 00:30 - 08:30
					[value: 20, intervalType: 1, startDate:[hour:8, minute:30], endDate:[hour: 15, minute:30]], // 08:30 - 15:30
					[value: 30, intervalType: 1, startDate:[hour:15, minute:29], endDate:[hour: 0, minute:15]]  // 15:30 - 00:15
				]
			]
		])

		Map inputValues = [:]
		Map outputValues = [
			value: [10, 20, 20, 30, 100]*.doubleValue()
		]
		Map channelMessages = [
			schedulerChannel: [
				[activeRules: [0]],
				[activeRules: [1]],
				[activeRules: [1,2]],
				[activeRules:[2]],
				[activeRules:[]]
			]
		]
		Map<Integer, Date> ticks = [
			1: "2015-04-05 07:05:00",
			2: "2015-04-05 11:30:00",
			3: "2015-04-05 18:29:00",
			4: "2015-04-05 18:30:00",
			5: "2015-04-06 03:15:00"
		].collectEntries() { Integer key, String value -> [(key): df.parse(value)] }

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.ticks(ticks)
			.extraIterationsAfterInput(6)
			.test()
	}
}