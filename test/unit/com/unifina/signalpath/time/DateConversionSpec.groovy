package com.unifina.signalpath.time


import com.unifina.domain.User
import com.unifina.signalpath.Input
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock
import spock.lang.Specification

import java.text.SimpleDateFormat

@Mock(User)
class DateConversionSpec extends Specification {

	def final static format = "yyyy-MM-dd HH:mm:ss"

	DateConversion module

	private void initContext(String username="username") {
		initContextWithUser(new User(username: username).save(failOnError: true, validate: false))
	}

	private void initContextWithUser(User user) {
		module = new DateConversion()
		module.globals = new Globals([:], user)
		module.init()
		module.connectionsReady()
	}

	void "dateConversion gives the right answer"() {
		initContext("username2")
		when:
		module.getInput("format").receive("yyyy-MM-dd HH:mm:ss")
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
		cal.setTimeInMillis(1444905310000)
		SimpleDateFormat df = new SimpleDateFormat(format)
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
		Map inputValues = [
			date: [
				df.format(cal.getTime()), // Thu Oct 15 10:35:10 UTC 2015 -> EEE MMM dd HH:mm:ss z yyyy
				"2000-01-01 12:45:55", // Sat Jan 01 12:45:55 UTC 2000
				Double.valueOf(1000 * 60 * 15) // +15 minutes to epoch: Thu Jan 01 00:15:00 UTC 1970
			],
		]
		Map outputValues = [
			date: [
				"2015-10-15 10:35:10",
				"2000-01-01 12:45:55",
				df.format(new Date(1000 * 60 * 15)),
			],
			ts: [
				1444905310000,
				946730755000,
				1000 * 60 * 15
			].collect { it?.doubleValue() },
			dayOfWeek: ["Thu", "Sat", "Thu"],
			years: [2015, 2000, 1970].collect { it?.doubleValue() },
			months: [10, 1, 1].collect { it?.doubleValue() },
			days: [15, 1, 1].collect { it?.doubleValue() },
			hours: [10,
					12,
					0
			].collect { it?.doubleValue() },
			minutes: [35, 45, 15].collect { it?.doubleValue() },
			seconds: [10, 55, 0].collect { it?.doubleValue() },
			milliseconds: [0, 0, 0].collect { it?.doubleValue() }
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "timestamp output must be correct from date input"() {
		initContext()

		when: "time is set and asked"
		Date date = new Date()
		module.getInput("date").receive(date);
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("ts").getValue() == date.getTime()
		module.getOutput("date").getValue() == null
	}

	void "timestamp output must be correct from ts input"() {
		initContext()

		when: "time is set and asked"
		Date date = new Date()
		module.getInput("date").receive((Double)date.getTime());
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("ts").getValue() == date.getTime()
		module.getOutput("date").getValue() == null
	}

	void "timestamp output must be correct from string input (daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		module.getInput("format").receive("yyyy-MM-dd HH:mm:ss")
		module.getInput("date").receive("2015-07-15 06:32:00")
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("ts").getValue() == df.parse("2015-07-15 09:32:00").getTime()
		module.getOutput("date").getValue() == null
	}

	void "timestamp output must be correct from string input (no daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		module.getInput("format").receive("yyyy-MM-dd HH:mm:ss")
		module.getInput("date").receive("2015-01-15 06:32:00")
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("ts").getValue() == df.parse("2015-01-15 08:32:00").getTime()
		module.getOutput("date").getValue() == null
	}

	def testRuntimeException(Closure c){
		try {
			c();
			return false
		} catch (RuntimeException e) {
			return true
		}
	}

	void "must throw exception when the given string is not in right format"() {
		initContext()

		when: "time is set and asked"
		module.getInput("format").receive("yyyy-MM-dd HH:mm:ss")
		module.getInput("date").receive("15/07/2015 09:32:00")
		then: "it throws exception"
		testRuntimeException{module.sendOutput()}
	}

	void "string output must be correct from date input (daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked without giving a format"
		Date date = df.parse("2015-07-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new DateConversion(), "in", "Object"))
		module.getInput("date").receive(date)
		module.sendOutput()

		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-07-15 06:32:00 UTC"

		when: "time is set and asked with a format"
		date = df.parse("2015-07-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.getInput("date").receive(date)
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/07/15 06:32"
	}

	void "string output must be correct from date input (no daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked without giving a format"
		Date date = df.parse("2015-01-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new DateConversion(), "in", "Object"))
		module.getInput("date").receive(date)
		module.sendOutput()

		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-01-15 07:32:00 UTC"

		when: "time is set and asked with a format"
		date = df.parse("2015-01-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.getInput("date").receive(date)
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/01/15 07:32"
	}

	void "string output must be correct from ts input (daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked without giving a format"
		Date date = df.parse("2015-07-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new DateConversion(), "in", "Object"))
		module.getInput("date").receive((Double)date.getTime())
		module.sendOutput()

		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-07-15 06:32:00 UTC"

		when: "time is set and asked with a format"
		date = df.parse("2015-07-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.getInput("date").receive((Double)date.getTime())
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/07/15 06:32"
	}

	void "string output must be correct from ts input (no daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked without giving a format"
		Date date = df.parse("2015-01-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new DateConversion(), "in", "Object"))
		module.getInput("date").receive((Double)date.getTime())
		module.sendOutput()

		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-01-15 07:32:00 UTC"

		when: "time is set and asked with a format"
		date = df.parse("2015-01-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.getInput("date").receive((Double)date.getTime())
		module.sendOutput()

		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/01/15 07:32"
	}

	void "years, months etc. outputs must work correctly from date input (daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		Date date = df.parse("2015-07-15 09:32:00.600")
		module.getInput("date").receive(date)
		module.sendOutput()

		then: "the values are correct"
		module.getOutput("dayOfWeek").getValue() == "Wed"
		module.getOutput("years").getValue() == 2015
		module.getOutput("months").getValue() == 7
		module.getOutput("days").getValue() == 15
		module.getOutput("hours").getValue() == 6
		module.getOutput("minutes").getValue() == 32
		module.getOutput("seconds").getValue() == 0
		module.getOutput("milliseconds").getValue() == 600
	}

	void "years, months etc. outputs must work correctly from date input (no daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		Date date = df.parse("2015-01-15 09:32:00.600")
		module.getInput("date").receive(date)
		module.sendOutput()

		then: "the values are correct"
		module.getOutput("hours").getValue() == 7
	}

	void "years, months etc. outputs must work correctly from ts input with a different timezone (daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		Date date = df.parse("2015-07-15 09:32:00")
		module.getInput("date").receive((Double)date.getTime())
		module.getInput("timezone").receive(TimeZone.getTimeZone("America/Argentina/Buenos_Aires")) // Argentina, used because there's no daylight saving time there
		module.sendOutput()

		then: "the values are correct"
		module.getOutput("hours").getValue() == 3
	}

	void "years, months etc. outputs must work correctly from ts input with a different timezone (no daylight saving)"() {
		initContext()

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		Date date = df.parse("2015-01-15 09:32:00")
		module.getInput("date").receive((Double)date.getTime())
		module.getInput("timezone").receive(TimeZone.getTimeZone("America/Argentina/Buenos_Aires")) // Argentina, used because there's no daylight saving time there
		module.sendOutput()

		then: "the values are correct"
		module.getOutput("hours").getValue() == 4
	}

	void "can be created without user"() {
		expect:
			initContextWithUser(null)
	}

}
