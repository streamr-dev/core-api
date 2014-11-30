package com.unifina.push;

import grails.converters.JSON;

import java.util.Map;

public class PushChannelMessage {

	private int counter;
	private String topic;
	private Object content;

	public PushChannelMessage(int counter, String topic, Object content) {
		this.counter = counter;
		this.topic = topic;
		this.content = content;
	}
	
	public String toJSON(JSON json) {
//		StringBuilder sb = new StringBuilder()
//		.append("[").append(counter).append(",")
//		.append("\"").append(topic).append("\"").append(",");
//		
//		if (content instanceof Map || content instanceof List) {
//			json.setTarget(content);
//			sb.append(json.toString());
//		}
//		else throw new IllegalArgumentException("content must be a Map!");
//			
//		sb.append("]");
//		return sb.toString();
		
		if (content instanceof Map) {
			((Map)content).put("counter", counter);
			json.setTarget(content);
			return json.toString();
		}
		else throw new IllegalArgumentException("content must be a Map!");
	}

	public int getCounter() {
		return counter;
	}

	public String getTopic() {
		return topic;
	}

	public Object getContent() {
		return content;
	}
	
}
