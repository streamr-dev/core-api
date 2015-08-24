package com.unifina.signalpath;

import java.util.LinkedHashMap;
import java.util.Map;

public class RuntimeResponse extends LinkedHashMap<String, Object> {
	
	public RuntimeResponse() {
		super();
	}
	
	public RuntimeResponse(Map<String, Object> response) {
		super();
		for (String key : response.keySet()) {
			this.put(key, response.get(key));
		}
	}
	
	public RuntimeResponse(boolean success, Map<String, Object> response) {
		super();
		for (String key : response.keySet()) {
			this.put(key, response.get(key));
		}
		setSuccess(success);
	}

	public boolean isSuccess() {
		return this.containsKey("success") && (Boolean) this.get("success");
	}
	
	public void setSuccess(boolean success) {
		this.put("success", success);
	}
	
}
