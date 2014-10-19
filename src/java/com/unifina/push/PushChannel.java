package com.unifina.push;

public abstract class PushChannel {
	private int counter = 0;
	private String channel;
	private boolean destroyed = false;
	
	public PushChannel(String channel) {
		this.channel = channel;
	}
	
	public void push(Object content) {
		PushChannelMessage msg = new PushChannelMessage(counter++, channel, content);
		doPush(msg);
	}
	
	protected abstract void doPush(PushChannelMessage msg);
	
	public String getChannel() {
		return channel;
	}
	
	public void destroy() {
		destroyed = true;
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}
}