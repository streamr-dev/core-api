package com.unifina.signalpath;

import java.util.HashMap;
import java.util.Map;

public abstract class ModuleExceptionMessage {
	protected int hash;

	ModuleExceptionMessage(int hash) {
		super();
		this.hash = hash;
	}

	/**
	 * For front-end JSON output.
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("module", hash);
		return map;
	}
}
