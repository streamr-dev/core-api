package com.unifina.signalpath.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilationErrorMessage extends HashMap<String, Object> {
	List<Map> errors = new ArrayList<>();
	
	public CompilationErrorMessage() {
		this.put("type","compilationErrors");
		this.put("errors",errors);
	}
	
	public void addError(long line, String message) {
		HashMap<String,Object> e = new HashMap<>();
		e.put("line", line);
//		e.put("pos",position);
		e.put("msg", message);
		errors.add(e);
	}
}
