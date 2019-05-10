package com.unifina.signalpath;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ModuleExceptionMessage {
	protected int hash;

	ModuleExceptionMessage(int hash) {
		super();
		this.hash = hash;
	}

	/**
	 * For front-end JSON output. Override to add
	 * subclass-specific fields.
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("module", hash);
		return result;
	}
}
