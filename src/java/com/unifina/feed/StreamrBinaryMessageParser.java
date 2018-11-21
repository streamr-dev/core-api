package com.unifina.feed;

import com.unifina.data.StreamrBinaryMessage;

public class StreamrBinaryMessageParser implements MessageParser<StreamrBinaryMessage, StreamrMessage> {

	@Override
	public StreamrMessage parse(StreamrBinaryMessage raw) {
		return raw.toStreamrMessage();
	}

}
