package com.unifina.service;

public class InvalidUsernameAndPasswordException extends RuntimeException {
	public InvalidUsernameAndPasswordException(String message) {
		super(message);
	}
}
