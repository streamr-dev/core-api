package com.unifina.push;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class PushChannel {
	
	Set<String> channels = new HashSet<>();
	private boolean destroyed = false;
	protected ArrayList<PushChannelEventListener> eventListeners = new ArrayList<>();
	
	public static final Logger log = Logger.getLogger(PushChannel.class);
	
	public PushChannel() {
	}
	
	public void addChannel(String channel) {
		channels.add(channel);
	}
	
	public List<String> getChannels() {
		List<String> result = new ArrayList<>(channels.size());
		result.addAll(channels);
		return result;
	}
	
	public void push(Object content, String channel) {
		PushChannelMessage msg = new PushChannelMessage(channel, content);
		doPush(msg);
	}
	
	protected abstract void doPush(PushChannelMessage msg);
	
	public void destroy() {
		log.info("destroy() called! Channels: "+channels);
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