package com.unifina.data;

import java.util.Date;
import java.util.Iterator;

import com.unifina.feed.AbstractFeed;

public class FeedEvent<ContentType> implements Comparable<FeedEvent> {
	public Date timestamp;
	public ContentType content;
	public IEventRecipient recipient;
	public AbstractFeed feed;
	public Iterator<FeedEvent> iterator;
	
	public long queueTicket = 0;
	
	public FeedEvent() {
		
	}
	
	public FeedEvent(ContentType content, Date timestamp, IEventRecipient recipient) {
		this.content = content;
		this.timestamp = timestamp;
		this.recipient = recipient;
	}
	
	@Override
	public int compareTo(FeedEvent e) {
		int t = timestamp.compareTo(e.timestamp);
		if (t!=0) return t;
		else return Long.compare(queueTicket, e.queueTicket);
	}
	
	@Override
	public String toString() {
		return timestamp + " - "+"iterator: "+iterator+", content: "+content;
	}
}
