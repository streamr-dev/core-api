package com.unifina.feed.map;

import com.unifina.feed.ITimestamped;

import java.util.Date;
import java.util.Map;

public class MapMessage implements ITimestamped {
	public final Date timestamp;
	public final Map payload;
	
	public MapMessage(Date timestamp, Map payload) {
		this.timestamp = timestamp;
		this.payload = payload;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}
}
