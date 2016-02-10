package com.unifina.feed;

/**
 * Created by henripihkala on 09/02/16.
 */
public abstract class AbstractMessageSource<RawMessageClass, KeyClass> implements MessageSource<RawMessageClass, KeyClass> {

	protected MessageRecipient recipient;
	protected long expected = 0;

	public AbstractMessageSource(MessageRecipient<RawMessageClass, KeyClass> recipient) {
		this.recipient = recipient;
	}

	@Override
	public void setRecipient(MessageRecipient<RawMessageClass, KeyClass> recipient) {
		this.recipient = recipient;
	}

	@Override
	public void setExpectedCounter(long expected) {
		this.expected = expected;
	}

}
