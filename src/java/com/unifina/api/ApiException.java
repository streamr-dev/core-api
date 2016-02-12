package com.unifina.api;

/**
 * Exceptions thrown by ApiControllers
 *
 * These exceptions know how they want to be presented to API user.
 * They can directly manipulate the ApiError before it is sent.
 */
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

	public ApiError asApiError() {
		return new ApiError(getStatusCode(), getCode(), getMessage());
	}
}
