package com.unifina.signalpath;

import java.util.HashMap;
import java.util.Map;

public class JavaCompilerErrorMessage extends ModuleExceptionMessage {
	public JavaCompilerErrorMessage(final int moduleIdHash, final Map<String, Object> msg) {
		super(moduleIdHash, msg);
	}

	/**
	 * For front-end JSON output.
	 */
	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		return result;
	}
}
