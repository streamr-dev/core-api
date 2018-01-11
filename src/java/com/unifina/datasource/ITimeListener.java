package com.unifina.datasource;

import java.util.Date;

public interface ITimeListener {
	void setTime(Date time);

	/**
	 * How often to invoke setTime(Date) in seconds. Special case 0 = never.
	 */
	int tickRateInSec();
}
