package com.unifina.signalpath;

/**
 * This message is the final (ever) in the associated stream.
 */
public class ByeMessage extends SignalPathMessage {
	public ByeMessage() {
		this.put("_bye", true);
	}
}
