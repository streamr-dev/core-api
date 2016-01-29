package com.unifina.api;

public class ApiException extends RuntimeException {

	private final int statusCode;
	private final String code;
	private final String message;

	public ApiException(int statusCode, String code, String message) {
		this.statusCode = statusCode;
		this.code = code;
		this.message = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
