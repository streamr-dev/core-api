package com.unifina.feed;

/**
 * Created by henripihkala on 09/02/16.
 */
public class ParsedMessage<MessageClass, KeyClass> extends Message<MessageClass, KeyClass> {
	public ParsedMessage(MessageClass message) {
		super(null, message);
	}
}
