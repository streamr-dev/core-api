package com.unifina.api;

public class NotPermittedException extends ApiException {
	/** User whose access was denied */
	private String user;
	/** Type of the denied resource */
	private String type;
	/** ID of the denied resource */
	private String id;
	/** Operation that was forbidden */
	private String op;

	public NotPermittedException(String message, String user, String type, String id, String op) {
		super(403, "FORBIDDEN", message);
		this.user = user;
		this.type = type;
		this.id = id;
		this.op = op;
	}
	public NotPermittedException(String user, String type, String id, String op) {
		this(generateMessage(user != null ? user : "Non-authenticated user", type, id, op), user, type, id, op);
	}
	public NotPermittedException(String user, String type, String id) {
		this(generateMessage(user != null ? user : "Non-authenticated user", type, id, "access"), user, type, id, null);
	}
	public NotPermittedException(String message) {
		this(message, null, null, null, null);
	}

	private static String generateMessage(String user, String type, String id, String op) {
		return user + " does not have permission to " + op + " " + type + " (id " + id + ")";
	}

	@Override
	public ApiError asApiError() {
		ApiError e = super.asApiError();
		if (type != null && id != null) {
			e.addToBody("user", user != null ? user : "<not authenticated>");
			e.addToBody("resource", type);
			e.addToBody("id", id);
			if (op == null) {
				e.addToBody("fault", "id");
			} else {
				e.addToBody("fault", "operation");
				e.addToBody("operation", op);
			}
		}
		return e;
	}

	public String getUser() {
		return user;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public String getOp() {
		return op;
	}
}
