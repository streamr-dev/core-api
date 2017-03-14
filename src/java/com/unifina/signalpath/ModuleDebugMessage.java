package com.unifina.signalpath;

import java.util.Date;

public class ModuleDebugMessage extends SignalPathMessage {
	public ModuleDebugMessage(String msg, AbstractSignalPathModule module) {
		this.put("type", "MD");
		this.put("moduleName", module.getEffectiveName());
		this.put("moduleHash", module.getHash());
		this.put("canvasTime", module.getGlobals().time);
		this.put("serverTime", new Date());
		this.put("msg", msg);
	}

	public ModuleDebugMessage(String msg, Endpoint endpoint) {
		this(msg, endpoint.getOwner());
		this.put("endpoint", endpoint.getId());
	}
}
