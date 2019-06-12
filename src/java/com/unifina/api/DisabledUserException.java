package com.unifina.api;

public class DisabledUserException extends RuntimeException {
	public DisabledUserException(String message) {
		super(message);
	}
}
