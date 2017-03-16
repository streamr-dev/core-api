package com.unifina.data;

import java.util.Date;

import com.unifina.feed.AbstractFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.ITimestamped;

public class FeedEvent<ContentClass extends ITimestamped, EventRecipientClass extends IEventRecipient>
		implements Comparable<FeedEvent<ContentClass, EventRecipientClass>> {

	public Date timestamp;
	public ContentClass content;
	public EventRecipientClass recipient;
	public AbstractFeed feed;
	public FeedEventIterator iterator;

	public long queueTicket = 0;

	public FeedEvent() {

	}

	public FeedEvent(ContentClass content, Date timestamp, EventRecipientClass recipient) {
		this.content = content;
		this.timestamp = timestamp;
		this.recipient = recipient;
	}

	@Override
	public int compareTo(FeedEvent<ContentClass, EventRecipientClass> e) {
		int t = timestamp.compareTo(e.timestamp);
		return (t != 0) ? t : Long.compare(queueTicket, e.queueTicket);
	}

	@Override
	public String toString() {
		return timestamp + " - " + "iterator: " + iterator + ", content: " + content;
	}
}
