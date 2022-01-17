package com.unifina.service;

public class InvalidSessionTokenException extends ApiException {
	public InvalidSessionTokenException(String message) {
		super(401, "INVALID_SESSION_TOKEN_ERROR", message);
	}
}
