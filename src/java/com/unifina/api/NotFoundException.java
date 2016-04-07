package com.unifina.api;

public class NotFoundException extends ApiException {
	/** Type of the missing resource */
	private String type;
	/** ID of the missing resource */
	private String id;

	public NotFoundException(String message, String type, String id) {
		super(404, "NOT_FOUND", message);
		this.type = type;
		this.id = id;
	}
	public NotFoundException(String type, String id) {
		this(type + " with id " + id + " not found", type, id);
	}
	public NotFoundException(String message) {
		this(message, null, null);
	}

	@Override
	public ApiError asApiError() {
		ApiError e = super.asApiError();
		if (type != null && id != null) {
			e.addEntry("type", type);
			e.addEntry("fault", "id");
			e.addEntry("id", id);
		}
		return e;
	}
}
