package com.unifina.signalpath.time;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Parameter;

import java.util.TimeZone;

public class TimezoneParameter extends Parameter<String> {

	public TimezoneParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
		super(owner, name, defaultValue, "String");
	}

	@Override
	public String parseValue(String timezoneID) {
		if (timezoneID == null) {
			return getValue();
		}
		return TimeZone.getTimeZone(timezoneID).getID();
	}

	@Override
	public Object formatValue(String value) {
		if (value == null) {
			return getValue();
		}
		return value;
	}
}
