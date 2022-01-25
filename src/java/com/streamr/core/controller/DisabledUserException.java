package com.streamr.core.controller;

import com.streamr.core.service.ApiException;

public class DisabledUserException extends ApiException {
	public DisabledUserException(String message) {
		super(401, "DISABLED_USER_EXCEPTION", message);
	}
}
