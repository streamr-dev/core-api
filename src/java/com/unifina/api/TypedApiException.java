package com.unifina.api;

public class TypedApiException extends ApiException {
	/** Type of the missing resource */
	private String type;
	/** ID of the missing resource */
	private String id;

	public TypedApiException(int statusCode, String code, String message, String type, String id) {
		super(statusCode, code, message);
		this.type = type;
		this.id = id;
	}

	@Override
	public ApiError asApiError() {
		ApiError e = super.asApiError();
		if (type != null && id != null) {
			e.addToBody("type", type);
			e.addToBody("fault", "id");
			e.addToBody("id", id);
		}
		return e;
	}
}
