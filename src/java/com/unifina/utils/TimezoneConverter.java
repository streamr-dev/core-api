package com.unifina.utils;

import java.util.Date;
import java.util.TimeZone;

public class TimezoneConverter {
	
	TimeZone tz;
	
	public TimezoneConverter(String timezone) {
		tz = TimeZone.getTimeZone(timezone);
	}
	
	public TimezoneConverter(TimeZone timezone) {
		tz = timezone;
	}
	
	public int getLocalToUTCOffset(long onWhatDay) {
		return -tz.getOffset(onWhatDay);
	}
	
	public int getLocalToUTCOffset(Date onWhatDay) {
		return -tz.getOffset(onWhatDay.getTime());
	}
	
	public int getUTCToLocalOffset(Date onWhatDay) {
		return tz.getOffset(onWhatDay.getTime());
	}
	
	public int getUTCToLocalOffset(long onWhatDay) {
		return tz.getOffset(onWhatDay);
	}
	

	public Date getFakeLocalTime(Date utc) {
		return new Date(utc.getTime() + getUTCToLocalOffset(utc));
	}
	
	/**
	 * When rendered in UTC time zone (server time zone, not user time zone),
	 * the time returned by this method looks like the local time of the user.
	 * Useful for rendering local times to the user while calculating them on 
	 * the server.
	 *  
	 * @param utc
	 * @return
	 */
	public long getFakeLocalTime(long utc) {
		return utc + getUTCToLocalOffset(utc);
	}
	
}
