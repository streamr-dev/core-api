package com.unifina.feed;

public interface IFeedCache<T> {
	
	public void cache(Object msg);
	public int getCacheSize();
	public Catchup<T> getCatchup();
	public void flush();
	
}
