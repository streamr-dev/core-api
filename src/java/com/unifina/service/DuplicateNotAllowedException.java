package com.unifina.service;

public class DuplicateNotAllowedException extends TypedApiException {
	public DuplicateNotAllowedException(String message, String type, String id) {
		super(400, "DUPLICATE_NOT_ALLOWED", message, type, id);
	}

	public DuplicateNotAllowedException(String type, String id) {
		this(type + " with id " + id + " already exists", type, id);
	}

	public DuplicateNotAllowedException(String message) {
		this(message, null, null);
	}
}
