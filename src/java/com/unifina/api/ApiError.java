package com.unifina.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents error object that will be sent to API user
 */
public class ApiError {
	private final int statusCode;
	private final Map<String, String> body = new HashMap<>();
	private final Map<String, String> headers;

	public ApiError(int statusCode, String code, String message) {
		this(statusCode, code, message, null);
	}
	public ApiError(int statusCode, String code, String message, Map<String, String> headers) {
		this.statusCode = statusCode;
		body.put("code", code);
		body.put("message", message);
		this.headers = headers;
	}

	public void addEntry(String key, String value) {
		body.put(key, value);
	}

	/** JSON object that is returned as message body */
	public Map<String, String> toMap() {
		return body;
	}

	public int getStatusCode() {
		return statusCode;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
}
