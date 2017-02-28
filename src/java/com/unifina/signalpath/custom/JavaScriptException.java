package com.unifina.signalpath.custom;

import javax.script.ScriptException;

class JavaScriptException extends RuntimeException {
	final int lineNumber;

	JavaScriptException(ScriptException e) {
		super(String.format("Line %d, Col %d: %s", e.getLineNumber(), e.getColumnNumber(),
			e.getCause().getMessage()));
		lineNumber = e.getLineNumber();
	}

	JavaScriptException(NoSuchMethodException e) {
		super(e.getMessage());
		lineNumber = -1;
	}
}
