package com.unifina.api;

import org.codehaus.groovy.runtime.typehandling.GroovyCastException;

import java.util.HashMap;
import java.util.Map;

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

	public Map toMap() {
		Map map = new HashMap();
		map.put("code", getCode());
		map.put("message", getMessage());
		return map;
	}
}
