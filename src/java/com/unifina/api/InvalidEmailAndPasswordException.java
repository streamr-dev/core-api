package com.unifina.api;

public class InvalidEmailAndPasswordException extends RuntimeException {
	public InvalidEmailAndPasswordException(String message) {
		super(message);
	}
}
