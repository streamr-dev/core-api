package com.unifina.push;

import grails.converters.JSON;

import java.util.Map;

public class PushChannelMessage {

	private String channel;
	private Object content;

	public PushChannelMessage(String channel, Object content) {
		this.channel = channel;
		this.content = content;
	}
	
	public String toJSON(JSON json) {
		if (content instanceof Map) {
			json.setTarget(content);
			return json.toString();
		}
		else throw new IllegalArgumentException("content must be a Map!");
	}

	public String getChannel() {
		return channel;
	}

	public Object getContent() {
		return content;
	}
	
}
