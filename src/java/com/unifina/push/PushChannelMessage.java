package com.unifina.push;

import grails.converters.JSON;

import java.util.Map;

public class PushChannelMessage {


	private final int ttl;
	private String channel;
	private Object content;

	public PushChannelMessage(String channel, Object content, int ttl) {
		this.channel = channel;
		this.content = content;
		this.ttl = ttl;
	}

	@Deprecated
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

	public int getTTL() {
		return ttl;
	}
}
