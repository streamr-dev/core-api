package com.unifina.signalpath.time

import spock.lang.Specification

import java.text.SimpleDateFormat

class RuleSpec extends Specification {
	TimeZone UTC = TimeZone.getTimeZone("UTC")
	TimeZone Helsinki = TimeZone.getTimeZone("Europe/Helsinki")

	Calendar now = Calendar.getInstance(UTC)
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

	void setup() {
		df.setTimeZone(Helsinki)
		now.setTime(df.parse("2015-08-07 09:52:42"))
	}

	void "getNext() [static method] works correctly (daylight saving)"() {

		HashMap<Integer, Integer> targets = [:]

		when: "targets is set"
		targets.put(Calendar.MINUTE, 0)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2015-08-07 10:00:00"


		when: "targets is set"
		targets = [:]
		targets.put(Calendar.MINUTE, 52)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2015-08-07 10:52:00"


		when: "targets is set"
		targets = [:]
		targets.put(Calendar.HOUR_OF_DAY, 12)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2015-08-07 15:00:00"


		when: "targets is set"
		targets = [:]
		targets.put(Calendar.DATE, 9)
		targets.put(Calendar.HOUR_OF_DAY, 9)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2015-08-09 12:00:00"


		when: "targets is set"
		targets = [:]
		targets.put(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
		targets.put(Calendar.HOUR_OF_DAY, 12)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2015-08-12 15:00:00"


		when: "targets is set"
		targets = [:]
		targets.put(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		targets.put(Calendar.MINUTE, 30)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2015-08-08 15:30:00"


		when: "targets is set"
		targets = [:]
		targets.put(Calendar.MONTH, 4)
		targets.put(Calendar.DATE, 16)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		targets.put(Calendar.MINUTE, 30)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2016-05-16 15:30:00"
	}


	void "getNext() [static method] works correctly (no daylight saving)"() {
		when: "targets is set"
		def targets = [:]
		targets.put(Calendar.MONTH, 0)
		targets.put(Calendar.DATE, 16)
		targets.put(Calendar.HOUR_OF_DAY, 12)
		targets.put(Calendar.MINUTE, 30)

		then: "getNext is correct"
		df.format(Rule.getNext(now, targets)) == "2016-01-16 14:30:00"
	}

	void "instantiateRule() instantiates and configures HOURLY rules correctly"() {
		when:
		def config = [
			intervalType: 0,
			value: 666,
			startDate: [minute: 3],
			endDate: [minute: 18]
		]
		def rule = Rule.instantiateRule(config, UTC)

		then:
		rule.class.simpleName == "HourlyRule"
		rule.config == config
	}

	void "instantiateRule() instantiates and configures DAILY rules correctly"() {
		when:
		def config = [
			intervalType: 1,
			value: 666,
			startDate: [hour: 13, minute: 3],
			endDate: [hour: 15, minute: 18]
		]
		def rule = Rule.instantiateRule(config, UTC)

		then:
		rule.class.simpleName == "DailyRule"
		rule.config == config
	}

	void "instantiateRule() instantiates and configures WEEKLY rules correctly"() {
		when:
		def config = [
			intervalType: 2,
			value: 666,
			startDate: [weekday: 2, hour: 13, minute: 3],
			endDate: [weekday: 5, hour: 15, minute: 18]
		]
		def rule = Rule.instantiateRule(config, UTC)

		then:
		rule.class.simpleName == "WeeklyRule"
		rule.config == config
	}

	void "instantiateRule() instantiates and configures MONTHLY rules correctly"() {
		when:
		def config = [
			intervalType: 3,
			value: 666,
			startDate: [day: 2, hour: 13, minute: 3],
			endDate: [day: 26, hour: 15, minute: 18]
		]
		def rule = Rule.instantiateRule(config, UTC)

		then:
		rule.class.simpleName == "MonthlyRule"
		rule.config == config
	}

	void "instantiateRule() instantiates and configures YEARLY rules correctly"() {
		when:
		def config = [
			intervalType: 4,
			value: 666,
			startDate: [month: 3, day: 2, hour: 13, minute: 3],
			endDate: [month: 9, day: 26, hour: 15, minute: 18]
		]
		def rule = Rule.instantiateRule(config, UTC)

		then:
		rule.class.simpleName == "YearlyRule"
		rule.config == config
	}

	def "isActive() and getNext() works as expected with HOURLY rules"() {
		def rule = Rule.instantiateRule([
		    intervalType: 0,
			value: 666,
			startDate: [minute: 5],
			endDate: [minute: 15]
		], UTC)

		expect:
		!rule.isActive(df.parse("2017-01-31 18:04:59"))
		rule.isActive(df.parse("2017-01-31 18:05:00"))
		rule.isActive(df.parse("2017-01-31 18:14:59"))
		!rule.isActive(df.parse("2017-01-31 18:15:00"))

		and:
		df.format(rule.getNext(df.parse("2017-01-31 18:04:59"))) == "2017-01-31 18:05:00"
		df.format(rule.getNext(df.parse("2017-01-31 18:05:00"))) == "2017-01-31 18:15:00"
		df.format(rule.getNext(df.parse("2017-01-31 18:14:59"))) == "2017-01-31 18:15:00"
		df.format(rule.getNext(df.parse("2017-01-31 18:15:00"))) == "2017-01-31 19:05:00"
	}

	def "isActive() and getNext() works as expected with DAILY rules"() {
		df.setTimeZone(UTC)

		def rule = Rule.instantiateRule([
			intervalType: 1,
			value: 666,
			startDate: [hour: 10, minute: 3],
			endDate: [hour: 12, minute: 13]
		], UTC)

		expect:
		!rule.isActive(df.parse("2017-01-31 10:02:59"))
		rule.isActive(df.parse("2017-01-31 10:03:00"))
		rule.isActive(df.parse("2017-01-31 12:12:59"))
		!rule.isActive(df.parse("2017-01-31 12:13:00"))

		and:
		df.format(rule.getNext(df.parse("2017-01-31 10:02:59"))) == "2017-01-31 10:03:00"
		df.format(rule.getNext(df.parse("2017-01-31 10:03:00"))) == "2017-01-31 12:13:00"
		df.format(rule.getNext(df.parse("2017-01-31 12:12:59"))) == "2017-01-31 12:13:00"
		df.format(rule.getNext(df.parse("2017-01-31 12:13:00"))) == "2017-02-01 10:03:00" // Tomorrow
	}

	def "isActive() and getNext() works as expected with WEEKLY rules"() {
		df.setTimeZone(UTC)

		def rule = Rule.instantiateRule([
			intervalType: 2,
			value: 666,
			startDate: [weekday: Calendar.TUESDAY, hour: 10, minute: 3],
			endDate: [weekday: Calendar.WEDNESDAY, hour: 12, minute: 13]
		], UTC)

		expect:
		!rule.isActive(df.parse("2017-01-31 10:02:59")) // Tue
		rule.isActive(df.parse("2017-01-31 10:03:00"))  // Tue
		rule.isActive(df.parse("2017-02-01 12:12:59"))  // Wed
		!rule.isActive(df.parse("2017-02-01 12:13:00")) // Wed

		and:
		df.format(rule.getNext(df.parse("2017-01-31 10:02:59"))) == "2017-01-31 10:03:00"
		df.format(rule.getNext(df.parse("2017-01-31 10:03:00"))) == "2017-02-01 12:13:00"
		df.format(rule.getNext(df.parse("2017-02-01 12:12:59"))) == "2017-02-01 12:13:00"
		df.format(rule.getNext(df.parse("2017-02-01 12:13:00"))) == "2017-02-07 10:03:00" // Next tuesday
	}

	def "isActive() and getNext() works as expected with MONTHLY rules"() {
		df.setTimeZone(UTC)

		def rule = Rule.instantiateRule([
			intervalType: 3,
			value: 666,
			startDate: [day: 5, hour: 10, minute: 3],
			endDate: [day: 6, hour: 12, minute: 13]
		], UTC)

		expect:
		!rule.isActive(df.parse("2017-02-05 10:02:59"))
		rule.isActive(df.parse("2017-02-05 10:03:00"))
		rule.isActive(df.parse("2017-02-06 12:12:59"))
		!rule.isActive(df.parse("2017-02-06 12:13:00"))

		and:
		df.format(rule.getNext(df.parse("2017-02-05 10:02:59"))) == "2017-02-05 10:03:00"
		df.format(rule.getNext(df.parse("2017-02-05 10:03:00"))) == "2017-02-06 12:13:00"
		df.format(rule.getNext(df.parse("2017-02-06 12:12:59"))) == "2017-02-06 12:13:00"
		df.format(rule.getNext(df.parse("2017-02-06 12:13:00"))) == "2017-03-05 10:03:00" // 5th of next month

	}

	def "isActive() and getNext() works as expected with YEARLY rules"() {
		df.setTimeZone(UTC)

		def rule = Rule.instantiateRule([
			intervalType: 4,
			value: 666,
			startDate: [month: 0, day: 5, hour: 10, minute: 3],
			endDate: [month: 2, day: 6, hour: 12, minute: 13]
		], UTC)

		expect:
		!rule.isActive(df.parse("2017-01-05 10:02:59"))
		rule.isActive(df.parse("2017-01-05 10:03:00"))
		rule.isActive(df.parse("2017-02-11 23:00:00"))
		rule.isActive(df.parse("2017-03-06 12:12:59"))
		!rule.isActive(df.parse("2017-03-06 12:13:00"))

		and:
		df.format(rule.getNext(df.parse("2017-01-05 10:02:59"))) == "2017-01-05 10:03:00"
		df.format(rule.getNext(df.parse("2017-01-05 10:03:00"))) == "2017-03-06 12:13:00"
		df.format(rule.getNext(df.parse("2017-02-11 23:00:00"))) == "2017-03-06 12:13:00"
		df.format(rule.getNext(df.parse("2017-03-06 12:12:59"))) == "2017-03-06 12:13:00"
		df.format(rule.getNext(df.parse("2017-03-06 12:13:00"))) == "2018-01-05 10:03:00" // January 5th of next year
	}
}