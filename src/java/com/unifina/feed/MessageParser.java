package com.unifina.feed;

public interface MessageParser<T> {
	public T parse(Object raw);
}
