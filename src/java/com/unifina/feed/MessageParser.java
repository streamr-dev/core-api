package com.unifina.feed;

public interface MessageParser<T,V> {
	public V parse(T raw);
}
