package com.unifina.api;

public class InvalidAPIKeyException extends RuntimeException {
	public InvalidAPIKeyException(String message) {
		super(message);
	}
}
