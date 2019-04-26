package com.unifina.signalpath.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilationErrorMessage extends HashMap<String, Object> {
	List<Map> errors = new ArrayList<>();
	
	public CompilationErrorMessage() {
		this.put("type", "compilationErrors");
		this.put("errors", this.errors);
	}
	
	public void addError(long line, String message) {
		final HashMap<String, Object> e = new HashMap<>();
		e.put("line", line);
		//e.put("pos", position);
		e.put("msg", message);
		this.errors.add(e);
	}
}
