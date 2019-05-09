package com.unifina.signalpath;

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
	public abstract Map<String, Object> toMap();
}
