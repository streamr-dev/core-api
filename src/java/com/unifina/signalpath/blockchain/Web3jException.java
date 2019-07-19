package com.unifina.signalpath.blockchain;

import org.web3j.protocol.core.Response;

public class Web3jException extends RuntimeException {
	private final int code;
	private final String data;
	private final String message;

	public Web3jException(Response.Error error) {
		this.code = error.getCode();
		this.data = error.getData();
		this.message = error.getMessage();
	}
}
