package com.streamr.core.service;

public class FieldCannotBeUpdatedException extends ApiException {
	public FieldCannotBeUpdatedException(String message) {
		super(422, "FIELD_CANNOT_BE_UPDATED", message);
	}
}
