package com.unifina.signalpath;

public class NotificationMessage extends SignalPathMessage {
	public NotificationMessage(String msg) {
		this.put("type","N");
		this.put("msg",msg);
		this.cacheId = "notification";
	}
}
