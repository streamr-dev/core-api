package com.unifina.feed;

import com.unifina.domain.data.Feed;

import java.util.Map;

public abstract class AbstractMessageSource<RawMessageClass, KeyClass> implements MessageSource<RawMessageClass, KeyClass> {
	private final Feed feed;
	private final Map<String, Object> config;
	private MessageRecipient<RawMessageClass, KeyClass> recipient;

	public AbstractMessageSource(Feed feed, Map<String, Object> config) {
		this.feed = feed;
		this.config = config;
	}

	@Override
	public void setRecipient(MessageRecipient<RawMessageClass, KeyClass> recipient) {
		this.recipient = recipient;
	}

	protected void forward(Message<RawMessageClass, KeyClass> msg) {
		recipient.receive(msg);
	}

	public Feed getFeed() {
		return feed;
	}

	public Map<String, Object> getConfig() {
		return config;
	}

	public MessageRecipient<RawMessageClass, KeyClass> getRecipient() {
		return recipient;
	}
}
