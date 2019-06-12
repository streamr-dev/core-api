package com.unifina.api;

public class InvalidUsernameAndPasswordException extends RuntimeException {
	public InvalidUsernameAndPasswordException(String message) {
		super(message);
	}
}
