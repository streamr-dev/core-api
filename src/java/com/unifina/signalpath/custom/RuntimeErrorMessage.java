package com.unifina.signalpath.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RuntimeErrorMessage extends HashMap<String, Object> {
	private final List<Map> errors = new ArrayList<>();

	RuntimeErrorMessage() {
		this.put("type", "runtimeErrors");
		this.put("errors", errors);
	}

	RuntimeErrorMessage(long line, String message) {
		this();
		addError(line, message);
	}

	void addError(long line, String message) {
		HashMap<String,Object> e = new HashMap<>();
		e.put("line", line);
		e.put("msg", message);
		errors.add(e);
	}
}
