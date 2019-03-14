package com.unifina.data;

import java.util.Date;

/**
 * This is just a dummy FeedEvent with no content and no recipient.
 * It is used to "tick" the clock in the realtime and historical event queues.
 */
public class ClockTickEvent extends FeedEvent {

	public ClockTickEvent(Date timestamp) {
		super(null, timestamp, null);
	}

}
