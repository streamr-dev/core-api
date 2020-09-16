package com.unifina.signalpath;

public class StreamNotFoundException extends RuntimeException {
	public StreamNotFoundException(String id) {
		super("Unknown stream id: " + id);
	}
}
