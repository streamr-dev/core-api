package com.unifina.feed;

import java.io.Closeable;

public interface MessageSource<RawMessageClass, KeyClass>  extends Closeable {
	public void setRecipient(MessageRecipient<RawMessageClass, KeyClass> recipient);
	public void subscribe(KeyClass key);
	public void unsubscribe(KeyClass key);
}
