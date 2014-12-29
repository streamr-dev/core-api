package com.unifina.push;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public abstract class PushChannel {
	
	private HashMap<String,Counter> counterByChannel = new HashMap<>();
	private boolean destroyed = false;
	protected ArrayList<PushChannelEventListener> eventListeners = new ArrayList<>();
	
	public static final Logger log = Logger.getLogger(PushChannel.class);
	
	public PushChannel() {
	}
	
	public void addChannel(String channel) {
		counterByChannel.put(channel, new Counter(0));
	}
	
	public void push(Object content, String channel) {
		PushChannelMessage msg = new PushChannelMessage(counterByChannel.get(channel).getAndIncrement(), channel, content);
		doPush(msg);
	}
	
	protected abstract void doPush(PushChannelMessage msg);
	
	public void destroy() {
		log.info("destroy() called");
		destroyed = true;
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}
	
	public void addEventListener(PushChannelEventListener l) {
		if (!eventListeners.contains(l))
			eventListeners.add(l);
	}
	
	class Counter {
		int counter;
		public Counter(int counter) {
			this.counter = counter;
		}
		public int getAndIncrement() {
			return counter++;
		}
	}
}