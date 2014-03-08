package com.unifina.feed.kafka;

import java.util.Date;
import java.util.Map;

public class MapMessage {
	public Date timestamp;
	public Date receiveTime;
	public Map content;
	
	public MapMessage(Date timestamp, Date receiveTime,
			Map content) {
		this.timestamp = timestamp;
		this.receiveTime = receiveTime;
		this.content = content;
	}
}
