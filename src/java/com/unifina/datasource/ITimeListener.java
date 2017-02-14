package com.unifina.datasource;

import java.util.Date;

public interface ITimeListener {
	void setTime(Date time);
	int tickRateInSec();
}
