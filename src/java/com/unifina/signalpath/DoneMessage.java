package com.unifina.signalpath;

public class DoneMessage extends SignalPathMessage {
	public DoneMessage() {
		this.put("type","D");
	}
}
