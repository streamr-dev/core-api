package com.unifina.feed;

import com.unifina.data.FeedEvent;

/**
 * A feed that can be caught up, ie. can produce a history of messages
 * since the beginning of the currently running real-time unit (eg. day).
 * 
 * If getNextEvent() returns null, it means that no more messages have been
 * received previously and that the next messages should be processed from 
 * the normal event queue.
 */
public interface ICatchupFeed {
	
	/**
	 * This method is used to poll the catchup events. Multiple events
	 * with the same timestamp can be returned at the same time. Null
	 * can be returned to signal the end of catchup events.
	 * 
	 * @return FeedEvents or null to signal the end of events
	 */
	FeedEvent[] getNextEvents();
}
