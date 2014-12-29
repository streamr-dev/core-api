package com.unifina.signalpath;

public class ErrorMessage extends SignalPathMessage {
	public ErrorMessage(String error) {
		this.put("type","E");
		this.put("error",error);
		this.cacheId = "error";
	}
}
