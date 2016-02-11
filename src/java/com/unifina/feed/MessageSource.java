package com.unifina.feed;

import java.io.Closeable;

public interface MessageSource<RawMessageClass, KeyClass>  extends Closeable {
	public void setRecipient(MessageRecipient<RawMessageClass, KeyClass> recipient);
	public void setExpectedCounter(long expected);
	public void subscribe(KeyClass subscriber);
	public void unsubscribe(KeyClass subscriber);
}
