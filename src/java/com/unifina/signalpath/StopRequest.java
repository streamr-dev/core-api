package com.unifina.signalpath;

import com.unifina.feed.ITimestamped;

import java.util.Date;

public class StopRequest implements ITimestamped {

	private final Date date;

	public StopRequest(Date date) {
		this.date = date;
	}

	@Override
	public Date getTimestamp() {
		return date;
	}
}
