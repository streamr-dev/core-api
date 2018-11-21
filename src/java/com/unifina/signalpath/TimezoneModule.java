package com.unifina.signalpath;

import java.util.TimeZone;

public interface TimezoneModule {
	void setTimezone(String timezone);
	TimeZone getTimezone();
}
