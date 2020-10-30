package com.unifina.service;

public class InvalidSessionTokenException extends RuntimeException {
	public InvalidSessionTokenException(String message) {
		super(message);
	}
}
