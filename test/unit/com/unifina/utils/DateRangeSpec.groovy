package com.unifina.utils

import spock.lang.Specification

class DateRangeSpec extends Specification {
	Date newBaseDay() {
		Calendar cal = Calendar.getInstance()
		cal.setTimeInMillis(0)
		cal.setTimeZone(DateRange.UTC)
		cal.set(2019, 0, 15, 0, 0, 0)
		Date date = cal.getTime()
		return date
	}

	def "parse int"() {
		setup:
		DateRange range = new DateRange(null, null)
		when:
		int i = range.parseInt("12")
		then:
		i == 12
	}

	def "parse non int"() {
		setup:
		DateRange range = new DateRange(null, null)
		when:
		int i = range.parseInt("A")
		then:
		i == 0
	}

	def "set base date"() {
		setup:
		Date day = newBaseDay()
		String start = "10:00:15"
		String end = "11:30:00"
		when:
		DateRange range = new DateRange(start, end)
		range.setBaseDate(day)
		then:
		range.getBeginTime() == 1547546415000
		range.getEndTime() == 1547551800000
	}

	def "set base date with start after end"() {
		setup:
		Date day = newBaseDay()
		String start = "10:00:15"
		String end = "09:30:00"
		when:
		DateRange range = new DateRange(start, end)
		range.setBaseDate(day)
		then:
		range.getBeginTime() == 1547460015000 /* -1 day from start day */
		range.getEndTime() == 1547544600000
	}

	def "is in range"(String start, String end, Date date, boolean result) {
		setup:
		DateRange range = new DateRange(start, end)
		range.setBaseDate(newBaseDay())
		expect:
		range.isInRange(date) == result
		where:
		start|end|date|result
		"19:00:00"|"22:00:00"|/*18:00*/new Date(1547575200000)|false
		"19:00:00"|"22:00:00"|/*21:00*/new Date(1547586000000)|true
		"19:00:00"|"22:00:00"|/*23:00*/new Date(1547593200000)|false
	}

	def "get midnight"() {
		setup:
		Calendar cal = Calendar.getInstance()
		cal.setTimeInMillis(0)
		cal.setTimeZone(DateRange.UTC)
		cal.set(2019, 0, 15, 10, 10, 10)

		when:
		Date midnight = DateRange.getMidnight(cal.getTime())

		then:
		midnight.getTime() == 1547510400000
	}
}
