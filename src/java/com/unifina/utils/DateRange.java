package com.unifina.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateRange {
	private final static TimeZone UTC = TimeZone.getTimeZone("UTC");

	private String timeOfDayStart;
	private String timeOfDayEnd;
	private long todayBegin = 0;
	private long todayEnd = 0;

	int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	Calendar parseDate(Date day, String time) {
		String values[] = time.split(":");
		int hour = parseInt(values[0]);
		int minute = parseInt(values[1]);
		int second = parseInt(values[2]);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(0);
		cal.setTimeZone(UTC);
		cal.setTime(day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	public DateRange(String timeOfDayStart, String timeOfDayEnd) {
		this.timeOfDayStart = timeOfDayStart;
		this.timeOfDayEnd = timeOfDayEnd;
	}

	public void setBaseDate(Date day) {
		Calendar start = parseDate(day, timeOfDayStart);
		Calendar end = parseDate(day, timeOfDayEnd);
		if (start.after(end)) {
			start.add(Calendar.DATE, -1);
		}
		this.todayBegin = start.getTime().getTime();
		this.todayEnd = end.getTime().getTime();
	}

	public long getBeginTime() {
		return todayBegin;
	}

	public long getEndTime() {
		return todayEnd;
	}

	public boolean isInRange(long time) {
		return time >= todayBegin && time <= todayEnd;
	}

	public boolean isInRange(Date time) {
		return isInRange(time.getTime());
	}

	public static Date getMidnight(Date day) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(0);
		cal.setTimeZone(UTC);
		cal.setTime(day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND,0);
		return cal.getTime();
	}
}
