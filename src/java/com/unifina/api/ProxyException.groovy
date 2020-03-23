package com.unifina.api

class ProxyException extends ApiException {
	final Map extraHeaders = null

	ProxyException(int code, String message, Map headers) {
		super(code, "PROXY_ERROR", message)
		extraHeaders = headers;
	}
	ProxyException(int code, String message) {
		super(code, "PROXY_ERROR", message)
	}
	ProxyException(String message) {
		super(500, "PROXY_ERROR", message)
	}

	@Override
	public ApiError asApiError() {
		return new ApiError(getStatusCode(), getCode(), getMessage(), extraHeaders)
	}
}
