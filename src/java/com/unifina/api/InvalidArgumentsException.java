package com.unifina.api;

import java.util.Map;

public class InvalidArgumentsException extends ApiException {
	/** Name of the faulty argument */
	private String fault;
	/** Value of the faulty argument */
	private String value;

	public InvalidArgumentsException(String message, String fault, String value) {
		super(400, "INVALID_ARGUMENTS", message);
		this.fault = fault;
		this.value = value;
	}
	public InvalidArgumentsException(String message) {
		this(message, null, null);
	}

	@Override
	public ApiError asApiError() {
		ApiError e = super.asApiError();
		if (fault != null && value != null) {
			e.addEntry("fault", fault);
			e.addEntry(fault, value);
		}
		return e;
	}
}
