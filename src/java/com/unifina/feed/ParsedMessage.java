package com.unifina.feed;

/**
 * Created by henripihkala on 09/02/16.
 */
public class ParsedMessage<MessageClass, KeyClass> extends Message<MessageClass, KeyClass> {
	public ParsedMessage(long counter, MessageClass message, boolean checkCounter) {
		super(null, counter, message, checkCounter);
	}
}
