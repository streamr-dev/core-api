package com.unifina.signalpath;

/**
 * This message notifies that SignalPath has stopped.
 */
public class DoneMessage extends SignalPathMessage {
	public DoneMessage() {
		this.put("type","D");
	}
}
