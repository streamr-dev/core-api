package com.unifina.signalpath.time

import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.signalpath.Input
import com.unifina.utils.Globals

@TestMixin(GrailsUnitTestMixin)
class ClockModuleSpec extends Specification {

	Globals globals
	ClockModule module
	
    def setup() {
		globals = new Globals([:], grailsApplication, new SecUser(timezone:"UTC", username: "username"))
		module = new ClockModule()
		module.globals = globals
		module.init()
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
	
	void "timestamp output must be correct"() {
		when: "time is set and asked"
		Date date = new Date()
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("timestamp").getValue() == date.getTime()
		module.getOutput("date").getValue() == null
	}
	
	void "string output works correctly (daylight saving)"() {
		when: "time is set and asked without giving a format"
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		Date date = df.parse("2015-07-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new ClockModule(), "in", "Object"))
		module.setTime(date)
		
		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-07-15 06:32:00 UTC"
		
		when: "time is set and asked with a format"
		date = df.parse("2015-07-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/07/15 06:32"
	}
	
	void "string output works correctly (not daylight saving)"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked without giving a format"
		Date date = df.parse("2015-01-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new ClockModule(), "in", "Object"))
		module.setTime(date)
		
		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-01-15 07:32:00 UTC"
		
		when: "time is set and asked with a format"
		date = df.parse("2015-01-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/01/15 07:32"
	}
	
}
