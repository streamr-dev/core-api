package com.unifina.service

import com.unifina.api.ApiException

class EthereumUserRequiredException extends ApiException {
	EthereumUserRequiredException() {
		super(400, "ETHEREUM_USER_REQUIRED", "Ethereum user account is required.")
	}
}
