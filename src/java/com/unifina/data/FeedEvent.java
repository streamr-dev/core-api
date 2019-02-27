package com.unifina.data;

import java.util.Date;
import com.unifina.feed.AbstractFeed;
import com.unifina.feed.FeedEventIterator;
import com.streamr.client.protocol.message_layer.ITimestamped;

public class FeedEvent<MessageClass extends ITimestamped, EventRecipientClass extends IEventRecipient>
		implements Comparable<FeedEvent<MessageClass, EventRecipientClass>> {

	public final Date timestamp;
	public final MessageClass content;
	public final EventRecipientClass recipient;
	public AbstractFeed feed;
	public FeedEventIterator<MessageClass, EventRecipientClass> iterator;

	public long queueTicket = 0;

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

	void deliver() {
		if (recipient != null) {
			recipient.receive(this);
		}
	}
}
