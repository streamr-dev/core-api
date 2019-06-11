package com.unifina.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleExceptionMessage {
	protected int moduleId;
	protected String message;

	/**
	 * @param moduleId Identifier of the module
	 * @param message Description of the error on the module
	 */
	public ModuleExceptionMessage(int moduleId, String message) {
		super();
		this.moduleId = moduleId;
		this.message = message;
	}

	/**
	 * Override to create special types of error messages,
	 * which can have module-specific handling in the frontend.
	 */
	protected String getType() {
		return "genericError";
	}

	/**
	 * For front-end JSON output. Override to add
	 * subclass-specific fields.
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("type", getType());
		result.put("module", moduleId);
		result.put("message", message);
		return result;
	}

	public int getModuleId() {
		return moduleId;
	}

	public String getMessage() {
		return message;
	}
}
