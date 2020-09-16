package com.unifina.signalpath.custom;

import java.util.Map;

public class JavaCompilerErrorMessage extends ModuleExceptionMessage {
	private final long line;

	public JavaCompilerErrorMessage(final int moduleId, final long line, String message) {
		super(moduleId, message);
		this.line = line;
	}

	@Override
	protected String getType() {
		return "compilerError";
	}

	/**
	 * For front-end JSON output.
	 */
	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = super.toMap();
		result.put("line", line);
		return result;
	}
}
