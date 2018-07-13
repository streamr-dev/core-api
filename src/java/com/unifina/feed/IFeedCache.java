package com.unifina.feed;

public interface IFeedCache<T> extends MessageRecipient {
	void flush();
}
