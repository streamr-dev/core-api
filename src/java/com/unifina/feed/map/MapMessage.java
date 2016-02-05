package com.unifina.feed.map;

import java.util.Date;
import java.util.Map;

public class MapMessage {
	public Date timestamp;
	public Date receiveTime;
	public Map payload;
	
	public MapMessage(Date timestamp, Date receiveTime,
			Map payload) {
		this.timestamp = timestamp;
		this.receiveTime = receiveTime;
		this.payload = payload;
	}
}
