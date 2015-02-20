package com.unifina.signalpath;

public class ModuleWarningMessage extends SignalPathMessage {
	public ModuleWarningMessage(String msg, int hash) {
		this.put("type", "MW");
		this.put("hash", hash);
		this.put("msg", msg);
	}
}
