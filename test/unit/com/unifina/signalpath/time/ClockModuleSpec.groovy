package com.unifina.signalpath.time

import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

import java.text.SimpleDateFormat

class ClockModuleSpec extends Specification {

	Globals globals
	ClockModule module
	
    def setup() {
		globals = new Globals([:], new SecUser(timezone:"UTC", username: "username"))
		globals.time = new Date(0)
		module = new ClockModule()
		module.globals = globals
		module.init()
		module.connectionsReady()
    }

	void "clockModule gives the right answer"() {
		when:
		module.getInput("format").receive("yyyy-MM-dd HH:mm:ss")
		Map inputValues = [:]
		Map outputValues = [
			date: [
				"1970-01-01 00:00:00",
				"1970-01-01 01:00:00",
				"1970-01-02 02:00:00",
				"1970-01-08 00:00:00"
			],
			timestamp: [
				0,
				1000 * 60 * 60,
				1000 * 60 * 60 * (24 + 2),
				1000 * 60 * 60 * 24 * 7
			].collect { it?.doubleValue() },
		]
		Map ticks = [
			1: new Date(0),
			2: new Date(1000 * 60 * 60),            // 1 hour since epoch
			3: new Date(1000 * 60 * 60 * (24 + 2)), // a day + 2 hours since epoch
			4: new Date(1000 * 60 * 60 * 24 * 7)    // 1 week since epoch
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.ticks(ticks)
			.extraIterationsAfterInput(5)
			.test()
	}

	void "clockModule gives the right answer when varying time units and rates"() {
		when:
		module.getInput("format").receive("HH:mm:ss")
		Map inputValues = [
			unit: [null, null, null, "MINUTE"] + (1..496).collect { null },
			rate: (1..120).collect { null } + [6] + (1..379).collect { null }
		]
		Map outputValues = [
			date:
				["00:00:00", "00:00:01", "00:00:02"] +
					(1..57).collect { "00:00:03" } +
					(1..60).collect { "00:01:00" } +
					(1..240).collect { "00:02:00" } +
					(1..140).collect { "00:06:00" },
			timestamp:
					([0, 1, 2] +
					(1..57).collect { 3 } +
					(1..60).collect { 60 } +
					(1..240).collect { 120 } +
					(1..140).collect { 360 }).collect { it.doubleValue() * 1000 },
		]
		Map everySecondTick = (0..499).collectEntries { Integer v -> [(v): new Date(v * 1000) ]}

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.ticks(everySecondTick)
			.extraIterationsAfterInput(4)
			.test()
	}
	
	void "timestamp output must be correct"() {
		when: "time is set and asked"
		Date date = new Date()
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("timestamp").getValue() == date.getTime()
	}
	
	void "string output works correctly (daylight saving)"() {
		when: "time is set and asked without giving a format"
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		Date date = df.parse("2015-07-15 09:32:00")
		module.setTime(date)
		
		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-07-15 06:32:00 UTC"
		
		when: "time is set and asked with a format"
		date = df.parse("2015-07-15 10:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/07/15 07:32"
	}
	
	void "string output works correctly (no daylight saving)"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked without giving a format"
		Date date = df.parse("2015-01-15 09:32:00")
		module.setTime(date)
		
		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-01-15 07:32:00 UTC"
		
		when: "time is set and asked with a format"
		date = df.parse("2015-01-15 10:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/01/15 08:32"
	}
}
