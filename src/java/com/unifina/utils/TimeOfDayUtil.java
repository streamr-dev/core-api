package com.unifina.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeOfDayUtil {
	
	
	private String timeOfDayStart;
	private String timeOfDayEnd;
	private TimezoneConverter tc;
	private static TimeZone utc = TimeZone.getTimeZone("UTC");
	
	long todayBegin = 0;
	long todayEnd = 0;
	
	public TimeOfDayUtil(String timeOfDayStart, String timeOfDayEnd, TimeZone userTimeZone) {
		this.timeOfDayStart = timeOfDayStart;
		this.timeOfDayEnd = timeOfDayEnd;
		tc = new TimezoneConverter(userTimeZone);
	}
	
	public String getStartString() {
		return timeOfDayStart;
	}
	
	public String getEndString() {
		return timeOfDayEnd;
	}
	
	public boolean hasBaseDate() {
		return todayBegin != 0;
	}
	
	public void clearBaseDate() {
		todayBegin = 0;
		todayEnd = 0;
	}
	
	public void setBaseDate(Date day) {
		try {
			String[] s = timeOfDayStart.split(":");
			String[] e = timeOfDayEnd.split(":");

			Calendar todBegin = new GregorianCalendar();
			todBegin.setTimeZone(utc);
			todBegin.setTime(day);

			todBegin.set(Calendar.HOUR_OF_DAY, s.length>0 ? Integer.parseInt(s[0]) : 0);
			todBegin.set(Calendar.MINUTE, s.length>1 ? Integer.parseInt(s[1]) : 0);
			todBegin.set(Calendar.SECOND, s.length>2 ? Integer.parseInt(s[2]) : 0);
			todBegin.set(Calendar.MILLISECOND,0);


			Calendar todEnd = new GregorianCalendar();
			todEnd.setTimeZone(utc);
			todEnd.setTime(day);
			todEnd.set(Calendar.HOUR_OF_DAY, e.length>0 ? Integer.parseInt(e[0]) : 0);
			todEnd.set(Calendar.MINUTE, e.length>1 ? Integer.parseInt(e[1]) : 0);
			todEnd.set(Calendar.SECOND, e.length>2 ? Integer.parseInt(e[2]) : 0);
			todEnd.set(Calendar.MILLISECOND,0);

			if (todBegin.after(todEnd))
				todBegin.add(Calendar.DATE,-1);

			todayBegin = todBegin.getTime().getTime() + tc.getLocalToUTCOffset(day);
			todayEnd = todEnd.getTime().getTime() + tc.getLocalToUTCOffset(day);

		} catch (Exception exception) {
			// No limit in case of exception
			Calendar todBegin = new GregorianCalendar();
			todBegin.setTimeZone(utc);
			todBegin.setTime(day);

			todBegin.set(Calendar.HOUR_OF_DAY, 0);
			todBegin.set(Calendar.MINUTE, 0);
			todBegin.set(Calendar.SECOND, 0);
			todBegin.set(Calendar.MILLISECOND,0);


			Calendar todEnd = new GregorianCalendar();
			todEnd.setTimeZone(utc);
			todEnd.setTime(day);
			todEnd.set(Calendar.HOUR_OF_DAY, 23);
			todEnd.set(Calendar.MINUTE, 59);
			todEnd.set(Calendar.SECOND, 59);
			todEnd.set(Calendar.MILLISECOND,999);

			if (todBegin.after(todEnd))
				todBegin.add(Calendar.DATE,-1);

			todayBegin = todBegin.getTime().getTime() + tc.getLocalToUTCOffset(day);
			todayEnd = todEnd.getTime().getTime() + tc.getLocalToUTCOffset(day);
		}
	}
	
	public long getBeginTime() {
		return todayBegin;
	}
	
	public long getEndTime() {
		return todayEnd;
	}
	
	public boolean isInRange(long time) {
		return time>=todayBegin && time<=todayEnd;
	}
	
	public boolean isInRange(Date time) {
		return isInRange(time.getTime());
	}
	
	public static Date getMidnight(Date day) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeZone(utc);
		cal.setTime(day);

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND,0);
		return cal.getTime();
	}
	
}
