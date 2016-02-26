package com.unifina.feed;

/**
 * Created by henripihkala on 09/02/16.
 */
public class ParsedMessage<RawMessageClass, MessageClass, KeyClass> extends Message<MessageClass, KeyClass> {

	public RawMessageClass rawMessage;

	public ParsedMessage(long counter, MessageClass message, RawMessageClass rawMessage) {
		super(counter, message);
		this.rawMessage = rawMessage;
	}

	public Object getRawMessage() {
		return rawMessage;
	}
}
