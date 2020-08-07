package com.unifina.signalpath.time

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.User
import com.unifina.signalpath.SignalPath
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock

import java.text.SimpleDateFormat

@Mock(User)
class SchedulerSpec extends UiChannelMockingSpecification {
	def setup() {
		mockServicesForUiChannels()
	}

	void "Scheduler works as expected"() {
		when:
		Scheduler module = new Scheduler()
        User user = new User(username: 'user').save(failOnError: true, validate: false)
		module = setupModule(module, [
			uiChannel: [id: "schedulerChannel"],
			schedule: [
				defaultValue: 100,
				rules: [
					[value: 10, intervalType: 1, startDate:[hour:0, minute:30], endDate:[hour: 8, minute:30]],  // 00:30 - 08:30
					[value: 20, intervalType: 1, startDate:[hour:8, minute:30], endDate:[hour: 15, minute:30]], // 08:30 - 15:30
					[value: 30, intervalType: 1, startDate:[hour:15, minute:29], endDate:[hour: 0, minute:15]]  // 15:30 - 00:15
				]
			]
		], new SignalPath(true), mockGlobals([:], user))

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

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
		df.setTimeZone(TimeZone.getTimeZone("EEST"))
		Map<Integer, Date> ticks = [
			1: "2015-04-05 07:05:00 EEST",
			2: "2015-04-05 11:30:00 EEST",
			3: "2015-04-05 18:29:00 EEST",
			4: "2015-04-05 18:30:00 EEST",
			5: "2015-04-06 03:15:00 EEST"
		].collectEntries() { Integer key, String value -> [(key): df.parse(value)] }

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			.ticks(ticks)
			.extraIterationsAfterInput(6)
			.test()
	}
}
