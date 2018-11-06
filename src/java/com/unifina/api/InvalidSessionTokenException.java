package com.unifina.api;

public class InvalidSessionTokenException extends RuntimeException {
	public InvalidSessionTokenException(String message) {
		super(message);
	}
}
