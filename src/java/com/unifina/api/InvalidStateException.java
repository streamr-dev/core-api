package com.unifina.api;

public class InvalidStateException extends ApiException {
	public InvalidStateException(String message) {
		super(500, "STATE_NOT_ALLOWED", message);
	}
}
