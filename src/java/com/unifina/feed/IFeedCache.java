package com.unifina.feed;

public interface IFeedCache<T> extends MessageRecipient {
	
//	public void cache(Object msg); replaced by receive(Message) in MessageRecipient
	public int getCacheSize();
	public Catchup<T> getCatchup();
	public void flush();
	
}
