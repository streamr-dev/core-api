package com.unifina.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents error object that will be sent to API user
 */
public class ApiError {
	private final int statusCode;
	private final Map<String, String> map = new HashMap<>();

	public ApiError(int statusCode, String code, String message) {
		this.statusCode = statusCode;
		map.put("code", code);
		map.put("message", message);
	}

	public void addEntry(String key, String value) {
		map.put(key, value);
	}

	/** JSON object that is returned as message body */
	public Map<String, String> toMap() {
		return map;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
