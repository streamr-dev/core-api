package com.unifina.service

class EthereumUserRequiredException extends ApiException {
	EthereumUserRequiredException() {
		super(400, "ETHEREUM_USER_REQUIRED", "Ethereum user account is required.")
	}
}
