package com.unifina.utils.testutils;

import com.unifina.data.StreamrBinaryMessage;
import com.unifina.domain.data.Feed;
import com.unifina.feed.Message;
import com.unifina.feed.MessageRecipient;
import com.unifina.feed.MessageSource;

import java.io.IOException;
import java.util.Map;

public class FakeMessageSource implements MessageSource {

	private MessageRecipient recipient;
	private long offset = 0;

	public FakeMessageSource(@SuppressWarnings("unused") Feed feed,
							 @SuppressWarnings("unused") Map<String, Object> config) {
	}

	@Override
	public void setRecipient(MessageRecipient recipient) {
		this.recipient = recipient;
	}

	@Override
	public void subscribe(Object key) {
	}

	@Override
	public void unsubscribe(Object subscriber) {
		throw new UnsupportedOperationException();
	}

	public void handleMessage(StreamrBinaryMessage rawMsg) {
		Message msg = new Message<>(rawMsg.getStreamId()+"-"+rawMsg.getPartition(), offset++, rawMsg, false);
		recipient.receive(msg);
	}

	@Override
	public void close() throws IOException {

	}
}
