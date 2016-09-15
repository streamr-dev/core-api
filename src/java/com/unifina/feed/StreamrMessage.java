package com.unifina.feed;

import java.util.Date;
import java.util.Map;

import com.unifina.feed.map.MapMessage;

public class StreamrMessage extends MapMessage {
	
	public String streamId;
	
	public StreamrMessage(String streamId, Date timestamp, Date receiveTime,
						  Map content) {
		super(timestamp,receiveTime,content);
		this.streamId = streamId;
	}
}
