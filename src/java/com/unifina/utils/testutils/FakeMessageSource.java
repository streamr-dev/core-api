package com.unifina.utils.testutils;

import com.unifina.domain.data.Feed;
import com.unifina.feed.Message;
import com.unifina.feed.MessageRecipient;
import com.unifina.feed.MessageSource;
import com.unifina.kafkaclient.UnifinaKafkaMessage;

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
	public void setExpectedCounter(long expected) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void subscribe(Object key) {
	}

	@Override
	public void unsubscribe(Object subscriber) {
		throw new UnsupportedOperationException();
	}

	public void handleMessage(UnifinaKafkaMessage kafkaMessage) {
		Message msg = new Message(kafkaMessage.getChannel(), offset++, kafkaMessage);
		msg.checkCounter = false;
		recipient.receive(msg);
	}

	@Override
	public void close() throws IOException {

	}
}
