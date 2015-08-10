package com.unifina.signalpath.time

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class SchedulerSpec extends Specification {
	
	Scheduler scheduler
	
	Calendar cal = Calendar.getInstance()
	Scheduler.Rule rule
	Date now = new Date(1438930362780) // Fri Aug 07 2015 09:52:42
	
	def setup() {
		scheduler = new Scheduler()
		scheduler.init()
		
		Map<String, Object> config = [:]
		config.defaultValue = 100
		config.rules = []
		config.rules.add([value: 10, intervalType: 0, startDate:[minute:00], endDate:[minute:10]])
		config.rules.add([value: 20, intervalType: 1, startDate:[hour:9, minute:30], endDate:[hour: 12, minute:45]])
		config.rules.add([value: 20, intervalType: 2, startDate:[weekday:0, hour:12, minute:30], endDate:[weekday:3, hour: 10, minute:45]])
		config.rules.add([value: 20, intervalType: 3, startDate:[day:1, hour:12, minute:30], endDate:[day:10, hour: 10, minute:45]])
		config.rules.add([value: 20, intervalType: 4, startDate:[month:1, day:4, hour:12, minute:30], endDate:[month: 2, day:1, hour: 10, minute:45]])
		
		scheduler.onConfiguration(config)
		
		rule = new Scheduler.Rule()
	}

	def cleanup() {
		
	}
	
	void "the rules getNext should work correctly"() {
		HashMap<Integer, Integer> targets = [:]
		when: "targets is set"
		targets.put(Calendar.MINUTE, 0)
		then: "getNext is correct"
		rule.getNext(now, targets).toString() == "gpsigjsdf"
	}
	
//	void "the scheduler rules should have correct configs"() {
//		expect:
//		scheduler.getRule(0).getConfig() == [value: 10, intervalType: 0, startDate:[minute:00], endDate:[minute:10]]
//		scheduler.getRule(3).getConfig() == [value: 20, intervalType: 3, startDate:[day:1, hour:12, minute:30], endDate:[day:10, hour: 10, minute:45]]
//	}
//	
//	void "the right rules should be active"() {
//		when: "now is set"
//		then: "2. and 4. rule should be active"
//		scheduler.getRule(0).isActive(now) == false
//		scheduler.getRule(1).isActive(now) == true
////		scheduler.getRule(2).isActive(now) == false
//		scheduler.getRule(3).isActive(now) == true
//		scheduler.getRule(4).isActive(now) == false
//	}
}