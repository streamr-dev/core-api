package com.unifina.feed;

public class Message<MessageClass, KeyClass> {
	public final KeyClass key;
	public final MessageClass message;

	public Message(MessageClass message) {
		this.key = null;
		this.message = message;
	}

	public Message(KeyClass key, MessageClass message) {
		this.key = key;
		this.message = message;
	}

	@Override
	public String toString() {
		return message.toString();
	}

}
