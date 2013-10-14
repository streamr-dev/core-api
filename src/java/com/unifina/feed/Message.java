package com.unifina.feed;

public class Message {
	public int counter;
	public Object message;
	public Object rawMessage;
	
	public Message(int counter, Object message) {
		this.counter = counter;
		this.message = message;
	}
	
	public Message(int counter, Object message, Object rawMessage) {
		this.counter = counter;
		this.message = message;
		this.rawMessage = rawMessage;
	}

	@Override
	public String toString() {
		return message.toString();
	}
	
	public Object getRawMessage() {
		return rawMessage;
	}
	
}
