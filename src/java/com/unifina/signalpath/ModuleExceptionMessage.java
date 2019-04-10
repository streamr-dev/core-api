package com.unifina.signalpath;

import java.util.List;
import java.util.Map;

public class ModuleExceptionMessage {
	public ModuleExceptionMessage(int hash, Map<String, Object> msg) {
		super();
		this.hash = hash;
		this.msg = msg;
	}
	private int hash;
	private Map<String,Object> msg;

	public List<Map> getErrors() {
		return (List<Map>) this.msg.get("errors");
	}
}
