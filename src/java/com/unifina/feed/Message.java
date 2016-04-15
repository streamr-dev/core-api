package com.unifina.feed;

public class Message<MessageClass, KeyClass> {
	public long counter;
	public MessageClass message;
	public boolean checkCounter = true;
	public KeyClass key;
	
	public Message(long counter, MessageClass message) {
		this.counter = counter;
		this.message = message;
	}
	
	public Message(KeyClass key, long counter, MessageClass message) {
		this.counter = counter;
		this.message = message;
		this.key = key;
	}

	@Override
	public String toString() {
		return message.toString();
	}
	
}
