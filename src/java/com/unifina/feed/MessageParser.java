package com.unifina.feed;

public interface MessageParser<RawMessageType,ParsedMessageType> {
	ParsedMessageType parse(RawMessageType raw);
}
