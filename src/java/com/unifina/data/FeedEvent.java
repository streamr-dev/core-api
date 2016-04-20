package com.unifina.data;

import java.util.Date;
import java.util.Iterator;

import com.unifina.feed.AbstractFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.ITimestamped;

public class FeedEvent<MessageClass extends ITimestamped, EventRecipientClass extends IEventRecipient>
		implements Comparable<FeedEvent<MessageClass, EventRecipientClass>> {

	public Date timestamp;
	public MessageClass content;
	public EventRecipientClass recipient;
	public AbstractFeed feed;
	public FeedEventIterator<MessageClass, EventRecipientClass> iterator;

	public long queueTicket = 0;

	public FeedEvent() {

	}

	public FeedEvent(MessageClass content, Date timestamp, EventRecipientClass recipient) {
		this.content = content;
		this.timestamp = timestamp;
		this.recipient = recipient;
	}

	@Override
	public int compareTo(FeedEvent<MessageClass, EventRecipientClass> e) {
		int t = timestamp.compareTo(e.timestamp);
		return (t != 0) ? t : Long.compare(queueTicket, e.queueTicket);
	}

	@Override
	public String toString() {
		return timestamp + " - " + "iterator: " + iterator + ", content: " + content;
	}
}
