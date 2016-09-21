package com.unifina.signalpath.time

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import java.text.SimpleDateFormat

import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.push.PushChannel
import com.unifina.utils.Globals

@TestMixin(GrailsUnitTestMixin)
class SchedulerSpec extends Specification {
	
	Scheduler scheduler
	
	Calendar cal = Calendar.getInstance()
	Scheduler.Rule rule
	Date now
	Globals globals
	PushChannel pushChannel
	
	def setup() {
		globals = new Globals([:], grailsApplication, new SecUser(timezone:"UTC", username: "username"))
		pushChannel = Mock(PushChannel)
		globals.uiChannel = pushChannel
		scheduler = new Scheduler()
		scheduler.globals = globals
		scheduler.init()
		
		Map<String, Object> config = [:]
		Map<String, Object> schedule = [:]
		config.put("schedule", schedule)
		schedule.defaultValue = 100
		schedule.rules = []
		schedule.rules.add([value: 10, intervalType: 0, startDate:[minute:55], endDate:[minute:05]])
		schedule.rules.add([value: 20, intervalType: 1, startDate:[hour:6, minute:30], endDate:[hour: 6, minute:59]])
		schedule.rules.add([value: 30, intervalType: 2, startDate:[weekday:2, hour:12, minute:30], endDate:[weekday:6, hour: 10, minute:45]])
		schedule.rules.add([value: 40, intervalType: 3, startDate:[day:1, hour:12, minute:30], endDate:[day:10, hour: 10, minute:45]])
		schedule.rules.add([value: 50, intervalType: 4, startDate:[month:1, day:4, hour:12, minute:30], endDate:[month: 2, day:1, hour: 10, minute:45]])
		
		scheduler.onConfiguration(config)
		
		rule = new Scheduler.Rule()
		rule.setTimeZone("UTC")
		now = new Date(1438930362780) // Fri Aug 07 2015 06:52:42 UTC
	}

	def cleanup() {
		
	}
	
	void "the rules getNext should work correctly (daylight saving)"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))

		HashMap<Integer, Integer> targets = [:]
		when: "targets is set"
		targets.put(Calendar.MINUTE, 0)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2015-08-07 10:00:00")
		
		when: "targets is set"
		targets = [:]
		targets.put(Calendar.MINUTE, 52)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2015-08-07 10:52:00")
		
		when: "targets is set"
		targets = [:]
		targets.put(Calendar.HOUR_OF_DAY, 12)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2015-08-07 15:00:00")
		
		when: "targets is set"
		targets.put(Calendar.DATE, 9)
		targets.put(Calendar.HOUR_OF_DAY, 9)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2015-08-09 12:00:00")
		
		when: "targets is set"
		targets = [:]
		targets.put(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2015-08-12 15:00:00")
		
		when: "targets is set"
		targets = [:]
		targets.put(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		targets.put(Calendar.MINUTE, 30)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2015-08-08 15:30:00")
		
		when: "targets is set"
		targets = [:]
		targets.put(Calendar.MONTH, 4)
		targets.put(Calendar.DATE, 16)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		targets.put(Calendar.MINUTE, 30)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2016-05-16 15:30:00")
	}
	
	void "the rules getNext should work correctly (no daylight saving)"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		
		HashMap<Integer, Integer> targets = [:]

		when: "targets is set"
		targets = [:]
		targets.put(Calendar.MONTH, 0)
		targets.put(Calendar.DATE, 16)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		targets.put(Calendar.MINUTE, 30)
		then: "getNext is correct"
		rule.getNext(now, targets) == df.parse("2016-01-16 14:30:00")
	}
	
	void "the scheduler rules should have correct schedules"() {
		expect:
		scheduler.getRule(0).getSchedule() == [value: 10, intervalType: 0, startDate:[minute:55], endDate:[minute:05]]
		scheduler.getRule(3).getSchedule() == [value: 40, intervalType: 3, startDate:[day:1, hour:12, minute:30], endDate:[day:10, hour: 10, minute:45]]
	}
	
	void "the right rules should be active"() {
		when: "now is set"
		then: "2. , 3. and 4. rule should be active"
		scheduler.getRule(0).isActive(now) == false
		scheduler.getRule(1).isActive(now) == true
		scheduler.getRule(2).isActive(now) == true
		scheduler.getRule(3).isActive(now) == true
		scheduler.getRule(4).isActive(now) == false
	}
	
	void "getMinimumNext should give right value"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		
		expect:
		scheduler.getMinimumNextTime(now) == df.parse("2015-08-07 09:55:00")
	}
	
	void "the module should send out the right value at the right time"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		scheduler = new Scheduler()
		scheduler.globals = globals
		scheduler.init()
		Map<String, Object> config = [:]
		Map<String, Object> schedule = [:]
		config.put("schedule", schedule)
		schedule.defaultValue = 100
		schedule.rules = []
		schedule.rules.add([value: 10, intervalType: 1, startDate:[hour:0, minute:30], endDate:[hour: 8, minute:30]])
		schedule.rules.add([value: 20, intervalType: 1, startDate:[hour:8, minute:30], endDate:[hour: 16, minute:30]])
		schedule.rules.add([value: 30, intervalType: 1, startDate:[hour:16, minute:30], endDate:[hour: 0, minute:15]])
		scheduler.onConfiguration(config)
		
		when: "setTime"
		now = df.parse("2015-04-05 07:05:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		scheduler.getOutput("value").getValue() == 10
		
		when: "setTime"
		now = df.parse("2015-04-05 11:30:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		scheduler.getOutput("value").getValue() == 20
		
		when: "setTime"
		now = df.parse("2015-04-05 19:30:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		scheduler.getOutput("value").getValue() == 30
		
		
		// No rule is active
		when: "setTime"
		now = df.parse("2015-04-06 03:15:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		scheduler.getOutput("value").getValue() == 100
	}
	
	void "the module should send out right activeRules"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		df.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"))
		scheduler = new Scheduler()
		scheduler.globals = globals
		scheduler.init()
		Map<String, Object> config = [:]
		Map<String, Object> schedule = [:]
		config.put("schedule", schedule)
		schedule.defaultValue = 100
		schedule.rules = []
		schedule.rules.add([value: 10, intervalType: 1, startDate:[hour:0, minute:30], endDate:[hour: 8, minute:30]])
		schedule.rules.add([value: 20, intervalType: 1, startDate:[hour:8, minute:30], endDate:[hour: 16, minute:30]])
		schedule.rules.add([value: 30, intervalType: 1, startDate:[hour:8, minute:30], endDate:[hour: 23, minute:30]])
		scheduler.onConfiguration(config)
		
		when: "setTime"
		now = df.parse("2015-04-05 07:05:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		1 * pushChannel.push([activeRules:[0]], scheduler.uiChannelId)
		
		when: "setTime"
		now = df.parse("2015-04-05 11:30:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		1 * pushChannel.push([activeRules:[1,2]], scheduler.uiChannelId)
		
		when: "setTime"
		now = df.parse("2015-04-05 19:30:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		1 * pushChannel.push([activeRules:[2]], scheduler.uiChannelId)
		
		
		// No rule is active
		when: "setTime"
		now = df.parse("2015-04-06 02:30:00")
		scheduler.setTime(now)
		then: "the output has the right value"
		1 * pushChannel.push([activeRules:[]], scheduler.uiChannelId)
	}
	
}