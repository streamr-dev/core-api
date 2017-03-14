package com.unifina.push;

public class PushChannelMessage {

	private final int ttl;
	private String channel;
	private Object content;

	public PushChannelMessage(String channel, Object content, int ttl) {
		this.channel = channel;
		this.content = content;
		this.ttl = ttl;
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
