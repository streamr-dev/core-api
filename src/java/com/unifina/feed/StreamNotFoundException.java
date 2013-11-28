package com.unifina.feed;

public class StreamNotFoundException extends RuntimeException {
	public StreamNotFoundException(String name) {
		super("Unknown stream name: "+name);
	}
	
	public StreamNotFoundException(Long id) {
		super("Unknown stream id: "+id);
	}
}
