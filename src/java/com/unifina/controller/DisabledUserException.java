package com.unifina.controller;

import com.unifina.service.ApiException;

public class DisabledUserException extends ApiException {
	public DisabledUserException(String message) {
		super(401, "DISABLED_USER_EXCEPTION", message);
	}
}
