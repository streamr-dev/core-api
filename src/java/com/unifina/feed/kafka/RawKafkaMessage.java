package com.unifina.feed.kafka;

import java.util.Date;

public class RawKafkaMessage {

	public String topic;
	public Date receiveTime;
	public byte[] content;
	
	public RawKafkaMessage(String topic, Date receiveTime, byte[] content) {
		this.topic = topic;
		this.receiveTime = receiveTime;
		this.content = content;
	}
	
}
