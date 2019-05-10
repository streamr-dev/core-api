package com.unifina.signalpath;

import java.util.Map;

public class JavaCompilerErrorMessage extends ModuleExceptionMessage {
	private final long line;
	private final String message;

	public JavaCompilerErrorMessage(final int moduleIdHash, final long line, final String message) {
		super(moduleIdHash);
		this.line = line;
		this.message = message;
	}

	/**
	 * For front-end JSON output.
	 */
	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = super.toMap();
		result.put("type", "compilerError");
		result.put("line", line);
		result.put("message", message);
		return result;
	}
}
