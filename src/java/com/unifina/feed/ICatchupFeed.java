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
	 * Signals the feed that catchup is starting. The feed may insert messages
	 * into the eventQueue, but the same events should not appear in the
	 * catchup set. The catchup events will be processed first and then the
	 * eventQueue will start processing normally.
	 * 
	 * @return true if catchup start succeeds, false otherwise. getNextEvents() should not be called if this returns false.
	 */
	boolean startCatchup();
	
	/**
	 * This method is used to poll the catchup events. Multiple events
	 * with the same timestamp can be returned at the same time. Null
	 * can be returned to signal the end of catchup events.
	 * 
	 * @return FeedEvents or null to signal the end of events
	 */
	FeedEvent[] getNextEvents();
	
	/**
	 * This method signals the end of catchup for this feed.
	 */
	void endCatchup();
}
