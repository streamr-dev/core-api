package com.unifina.signalpath.time;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Parameter;

import java.util.TimeZone;

public class TimezoneParameter extends Parameter<TimeZone> {

	public TimezoneParameter(AbstractSignalPathModule owner, String name, TimeZone defaultValue) {
		super(owner, name, defaultValue, "String");
	}

	@Override
	public TimeZone parseValue(String timezoneID) {
		if (timezoneID == null) {
			return getValue();
		}
		return TimeZone.getTimeZone(timezoneID);
	}

	@Override
	public Object formatValue(TimeZone value) {
		if (value == null) {
			return getValue();
		}
		return value;
	}
}
