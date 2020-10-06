package com.unifina.service;

public class BadRequestException extends ApiException {
	public BadRequestException(String message) {
		super(400, "PARAMETER_MISSING", message);
	}

	public BadRequestException(String code, String message) {
		super(400, code, message);
	}
}
