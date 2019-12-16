package com.unifina.api

class ProxyException extends ApiException {
	ProxyException(int code, String message) {
		super(code, "PROXY_ERROR", message)
	}

	ProxyException(String message) {
		super(500, "PROXY_ERROR", message)
	}
}
