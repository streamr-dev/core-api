package com.unifina.feed;

public interface MessageSource<RawMessageClass, KeyClass> {
	public void setRecipient(MessageRecipient<RawMessageClass, KeyClass> recipient);
	public void setExpectedCounter(long expected);
	public void subscribe(KeyClass subscriber);
	public void unsubscribe(KeyClass subscriber);
}
