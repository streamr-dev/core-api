package com.unifina.feed;

public class Message<MessageClass, KeyClass> {
	public final KeyClass key;
	public final long counter;
	public final MessageClass message;
	public final boolean checkCounter;

	public Message(long counter, MessageClass message) {
		this.key = null;
		this.counter = counter;
		this.message = message;
		this.checkCounter = true;
	}
	
	public Message(KeyClass key, long counter, MessageClass message) {
		this.key = key;
		this.counter = counter;
		this.message = message;
		this.checkCounter = true;
	}

	public Message(KeyClass key, long counter, MessageClass message, boolean checkCounter) {
		this.key = key;
		this.counter = counter;
		this.message = message;
		this.checkCounter = checkCounter;
	}

	@Override
	public String toString() {
		return message.toString();
	}
	
}
