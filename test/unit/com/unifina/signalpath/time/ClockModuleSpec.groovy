package com.unifina.signalpath.time

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

import spock.lang.Specification

import com.unifina.domain.signalpath.Module
import com.unifina.signalpath.Input

@TestMixin(GrailsUnitTestMixin)
class ClockModuleSpec extends Specification {
	
	ClockModule module
	
    def setup() {
		module = new ClockModule()
		module.init()
    }

    def cleanup() {
		
    }
	
	void "timestamp output must be correct"() {
		when: "time is set and asked"
		Date date = new Date()
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("timestamp").getValue() == date.getTime()
		module.getOutput("date").getValue() == null
	}
	
	void "string output works correctly"() {
		when: "time is set and asked without giving a format"
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-07-15 09:32:00")
		module.getOutput("date").connect(new Input<Object>(new ClockModule(), "in", "Object"))
		module.setTime(date)
		
		then: "the time is sent out with the default format"
		module.getOutput("date").getValue() == "2015-07-15 09:32:00 EEST"
		
		when: "time is set and asked with a format"
		date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-07-15 09:32:00")
		module.getInput("format").receive("yyyy/MM/dd HH:mm")
		module.setTime(date)
		
		then: "the time is sent out"
		module.getOutput("date").getValue() == "2015/07/15 09:32"
	}
	
}
