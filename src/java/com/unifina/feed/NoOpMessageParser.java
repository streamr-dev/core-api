package com.unifina.feed;

/**
 * Utility class for Feeds that do not have a raw message type that needs to be parsed.
 */
public class NoOpMessageParser<MessageType> implements MessageParser<MessageType, MessageType> {
	@Override
	public MessageType parse(MessageType raw) {
		return raw;
	}
}
