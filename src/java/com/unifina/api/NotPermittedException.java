package com.unifina.api;

public class NotPermittedException extends ApiException {
	/** User whose access was denied */
	private String user;
	/** Type of the denied resource */
	private String type;
	/** ID of the denied resource */
	private String id;

	public NotPermittedException(String message, String user, String type, String id) {
		super(403, "FORBIDDEN", message);
		this.user = user;
		this.type = type;
		this.id = id;
	}
	public NotPermittedException(String user, String type, String id) {
		this((user != null ? user : "Non-authenticated user") +
				" does not have permission to access " + type + " (id " + id + ")", user, type, id);
	}
	public NotPermittedException(String message) {
		this(message, null, null, null);
	}

	@Override
	public ApiError asApiError() {
		ApiError e = super.asApiError();
		if (type != null && id != null) {
			e.addEntry("user", user != null ? user : "<not authenticated>");
			e.addEntry("fault", "permissions");
			e.addEntry("resource", type);
			e.addEntry("id", id);
		}
		return e;
	}
}
