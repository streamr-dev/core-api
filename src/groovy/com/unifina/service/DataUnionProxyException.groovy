package com.unifina.service

class DataUnionProxyException extends ApiException {
	final Map extraHeaders = null

	DataUnionProxyException(int code, String message, Map headers) {
		super(code, "PROXY_ERROR", message)
		extraHeaders = headers;
	}

	DataUnionProxyException(int code, String message) {
		super(code, "PROXY_ERROR", message)
	}

	DataUnionProxyException(String message) {
		super(500, "PROXY_ERROR", message)
	}

	@Override
	public ApiError asApiError() {
		return new ApiError(getStatusCode(), getCode(), getMessage(), extraHeaders)
	}
}
