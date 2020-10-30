package com.unifina.service;

public class NotFoundException extends TypedApiException {
	public NotFoundException(String message, String type, String id) {
		super(404, "NOT_FOUND", message, type, id);
	}

	public NotFoundException(String type, String id) {
		this(type + " with id " + id + " not found", type, id);
	}

	public NotFoundException(String message) {
		this(message, null, null);
	}
}
