package com.unifina.service;

public class InvalidAPIKeyException extends RuntimeException {
	public InvalidAPIKeyException(String message) {
		super(message);
	}
}
