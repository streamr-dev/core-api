package com.unifina.signalpath;

import java.util.LinkedHashMap;
import java.util.Map;

public class RuntimeResponse extends LinkedHashMap<String, Object> {
	
	protected boolean success = false;
	
	public RuntimeResponse() {
		super();
	}
	
	public RuntimeResponse(Map<String, Object> response) {
		super();
		this.success = response.containsKey("success") && Boolean.parseBoolean(response.get("success").toString());
		for (String key : response.keySet()) {
			this.put(key, response.get(key));
		}
	}
	
	public RuntimeResponse(boolean success, Map<String, Object> response) {
		super();
		this.success = success;
		for (String key : response.keySet()) {
			this.put(key, response.get(key));
		}
	}

	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
