package com.unifina.exceptions;

public class FeedNotFoundException extends RuntimeException {
	public FeedNotFoundException(String name) {
		super("Unknown feed name: "+name);
	}
	
	public FeedNotFoundException(Long id) {
		super("Unknown feed id: "+id);
	}
}
