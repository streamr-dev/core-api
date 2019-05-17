package com.unifina.api;

public class BadRequestException extends ApiException {
	public BadRequestException(String message) {
		super(400, "PARAMETER_MISSING", message);
	}
}
