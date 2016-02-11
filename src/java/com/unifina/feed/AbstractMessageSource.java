package com.unifina.feed;

import com.unifina.domain.data.Feed;

import java.util.Map;

/**
 * Created by henripihkala on 09/02/16.
 */
public abstract class AbstractMessageSource<RawMessageClass, KeyClass> implements MessageSource<RawMessageClass, KeyClass> {

	private final Feed feed;
	private final Map<String, Object> config;
	protected MessageRecipient<RawMessageClass, KeyClass> recipient;
	protected long expected = 0;

	public AbstractMessageSource(Feed feed, Map<String,Object> config) {
		this.feed = feed;
		this.config = config;
	}

	@Override
	public void setRecipient(MessageRecipient<RawMessageClass, KeyClass> recipient) {
		this.recipient = recipient;
	}

	@Override
	public void setExpectedCounter(long expected) {
		this.expected = expected;
	}

	/**
	 * Sends a Message to the attached MessageRecipient. This method should be called
	 * once for each RawMessageClass instance received from the message source.
	 * @param content
	 * @param key
	 * @param counter
	 * @param checkCounter
     */
	protected void forward(RawMessageClass content, KeyClass key, long counter, boolean checkCounter) {
		Message<RawMessageClass, KeyClass> msg = new Message<>(key, counter, content);
		msg.checkCounter = checkCounter;
		recipient.receive(msg);
	}

	protected void forward(Message<RawMessageClass, KeyClass> msg) {
		recipient.receive(msg);
	}

}
