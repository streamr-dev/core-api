package com.unifina.feed;

public interface MessageSource {
	public void setRecipient(MessageRecipient recipient);
	public void setExpectedCounter(long expected);
	public void subscribe(Object subscriber);
	public void unsubscribe(Object subscriber);
}
