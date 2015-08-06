package com.unifina.signalpath.time

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class SchedulerSpec extends Specification {
	
	Scheduler scheduler
	Scheduler.Rule rule
	
	Calendar cal = Calendar.getInstance()
	
	def setup() {
		scheduler = new Scheduler()
		scheduler.init()
		
		Map<String, Object> config = [:]
		config.defaultValue = 100
		config.rules = []
//		config.rules.add([value: 10, intervalType: 0, startDate:[minute:30], endDate:[minute:45]])
		config.rules.add([value: 20, intervalType: 4, startDate:[month:1, day:4, hour:12, minute:30], endDate:[month: 2, day:1, hour: 10, minute:45]])
		
		scheduler.onConfiguration(config)
	}

	def cleanup() {
		
	}
	
	void "getNext should work correctly"() {
		Date now = new Date(1438866681934) // Thu Aug 06 2015 16:11:21
		rule = scheduler.getRules().get(0)
		when: 'time is set'
		cal.setTime(now)
		cal.set(Calendar.MINUTE, 0)
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.MILLISECOND, 0)
		Map<Integer, Integer> targets = new HashMap<>();
		targets.put(Calendar.MINUTE, 0)
		
		then: 'the getNext gives the correct answer'
		rule.getNext(now, targets) == cal.getTime()
	}	
}