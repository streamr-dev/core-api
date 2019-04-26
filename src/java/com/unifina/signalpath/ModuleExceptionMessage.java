package com.unifina.signalpath;

import java.util.Map;

public abstract class ModuleExceptionMessage {
	protected int hash;
	protected Map<String,Object> msg;

	ModuleExceptionMessage(int hash, Map<String, Object> msg) {
		super();
		this.hash = hash;
		this.msg = msg;
	}

	/**
	 * For front-end JSON output.
	 */
	public abstract Map<String, Object> toMap();
}
