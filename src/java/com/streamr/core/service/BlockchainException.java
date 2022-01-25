package com.streamr.core.service;

import javax.servlet.http.HttpServletResponse;

public class BlockchainException extends ApiException {
	protected static final int STATUS_CODE = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	protected static final String CODE = "BLOCKCHAIN_ERROR";

	public BlockchainException(String message) {
		super(STATUS_CODE, CODE, message);
	}
}
