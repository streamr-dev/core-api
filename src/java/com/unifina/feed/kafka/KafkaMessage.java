package com.unifina.feed.kafka;

import java.util.Date;
import java.util.Map;

import com.unifina.feed.map.MapMessage;

public class KafkaMessage extends MapMessage {
	
	public String topic;
	
	public KafkaMessage(String topic, Date timestamp, Date receiveTime,
			Map content) {
		super(timestamp,receiveTime,content);
		this.topic = topic;
	}
}
