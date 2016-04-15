package com.unifina.feed.map;

import com.unifina.feed.ITimestamped;

import java.util.Date;
import java.util.Map;

public class MapMessage implements ITimestamped {
	public Date timestamp;
	public Date receiveTime;
	public Map payload;
	
	public MapMessage(Date timestamp, Date receiveTime, Map payload) {
		this.timestamp = timestamp;
		this.receiveTime = receiveTime;
		this.payload = payload;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}
}
