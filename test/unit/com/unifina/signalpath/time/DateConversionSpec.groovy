package com.unifina.signalpath.time

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.signalpath.Input
import com.unifina.utils.Globals

@TestMixin(GrailsUnitTestMixin)
class DateConversionSpec extends Specification {
	
	Globals globals
	DateConversion module
	
    def setup() {
		globals = new Globals([:], grailsApplication, new SecUser(timezone:"UTC", username: "username"))
		module = new DateConversion()
		module.globals = globals
		module.init()
		module.connectionsReady()
    }

    def cleanup() {
		
    }
	
	void "timestamp output must be correct from date input"() {
		when: "time is set and asked"
		Date date = new Date()
		module.getInput("date").receive(date);
		module.sendOutput()
		
		then: "the time is sent out"
		module.getOutput("ts").getValue() == date.getTime()
		module.getOutput("date").getValue() == null
	}
	
	void "timestamp output must be correct from ts input"() {
		when: "time is set and asked"
		Date date = new Date()
		module.getInput("date").receive((Double)date.getTime());
		module.sendOutput()
		
		then: "the time is sent out"
		module.getOutput("ts").getValue() == date.getTime()
		module.getOutput("date").getValue() == null
	}
	
	void "timestamp output must be correct from string input (daylight saving)"() {
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
		when: "time is set and asked"
		module.getInput("format").receive("yyyy-MM-dd HH:mm:ss")
		module.getInput("date").receive("15/07/2015 09:32:00")
		then: "it throws exception"
		testRuntimeException{module.sendOutput()}
	}
	
	void "string output must be correct from date input (daylight saving)"() {
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
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		Date date = df.parse("2015-07-15 09:32:00")
		module.getInput("date").receive((Double)date.getTime())
		module.getInput("timezone").receive("America/Argentina/Buenos_Aires") // Argentina, used because there's no daylight saving time there
		module.sendOutput()
		
		then: "the values are correct"
		module.getOutput("hours").getValue() == 3
	}
	
	void "years, months etc. outputs must work correctly from ts input with a different timezone (no daylight saving)"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		when: "time is set and asked"
		Date date = df.parse("2015-01-15 09:32:00")
		module.getInput("date").receive((Double)date.getTime())
		module.getInput("timezone").receive("America/Argentina/Buenos_Aires") // Argentina, used because there's no daylight saving time there
		module.sendOutput()
		
		then: "the values are correct"
		module.getOutput("hours").getValue() == 4
	}
	
}
