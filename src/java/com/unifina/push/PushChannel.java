package com.unifina.push;

import org.apache.log4j.Logger;

public abstract class PushChannel {
	private int counter = 0;
	protected String channel;
	private boolean destroyed = false;
	
	public static final Logger log = Logger.getLogger(PushChannel.class);
	
	public PushChannel(String channel) {
		this.channel = channel;
		log.info("Created: "+channel);
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
		log.info("destroy() called: "+channel);
		destroyed = true;
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}
}