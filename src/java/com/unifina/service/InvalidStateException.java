package com.unifina.service;

public class InvalidStateException extends RuntimeException {
	public InvalidStateException(String message) {
		super(message);
	}
}
