package com.unifina.api

class ProxyException extends ApiException {
	ProxyException(String message) {
		super(500, "PROXY_ERROR", message)
	}
}
