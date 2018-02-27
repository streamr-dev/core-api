package com.unifina.api;

public class DuplicateNotAllowedException extends ApiException {
	public DuplicateNotAllowedException(String message) {
		super(400, "DUPLICATE_NOT_ALLOWED", message);
	}
}
