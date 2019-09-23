package com.unifina.data;

import com.streamr.client.protocol.message_layer.ITimestamped;

import java.util.Date;

/**
 * This is just a dummy event payload used to "tick" the clock in realtime and historical event queues.
 */
public class ClockTick implements ITimestamped {

	private final long timestamp;

	public ClockTick(Date timestamp) {
		this.timestamp = timestamp.getTime();
	}

	@Override
	public Date getTimestampAsDate() {
		return new Date(timestamp);
	}
}
