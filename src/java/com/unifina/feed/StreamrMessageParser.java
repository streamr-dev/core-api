package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;

public class StreamrMessageParser implements MessageParser<StreamMessage, StreamrMessage> {

	@Override
	public StreamrMessage parse(StreamMessage raw) {
		return new StreamrMessage(raw.getStreamId(), raw.getStreamPartition(), raw.getTimestampAsDate(), raw.getContent());
	}

}
