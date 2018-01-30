package com.unifina.exceptions;

public class StreamNotFoundException extends RuntimeException {
	public StreamNotFoundException(String id) {
		super("Unknown stream id: " + id);
	}
}
