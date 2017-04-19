package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A helper class to apply a static recipient and iterator to FeedEvents,
 * whose content is pulled from a separate content iterator.
 * @author Henri
 */
public class FeedEventIterator<MessageClass extends ITimestamped, EventRecipientClass extends IEventRecipient>
		implements Iterator<FeedEvent<MessageClass, EventRecipientClass>>, Closeable {

	private Iterator<MessageClass> contentIterator;
	private EventRecipientClass recipient;
	private AbstractHistoricalFeed feed;
	
	private final Logger log = Logger.getLogger(FeedEventIterator.class);
	
	public FeedEventIterator(Iterator<MessageClass> contentIterator, AbstractHistoricalFeed feed, EventRecipientClass recipient) {
		this.contentIterator = contentIterator;
		this.recipient = recipient;
		this.feed = feed;
	}
	
	@Override
	public boolean hasNext() {
		return contentIterator.hasNext();
	}

	@Override
	public FeedEvent<MessageClass, EventRecipientClass> next() {
		MessageClass content = contentIterator.next();
		if (content==null)
			return null;

		FeedEvent<MessageClass, EventRecipientClass> fe = new FeedEvent<>(content, content.getTimestamp(), recipient);
		fe.feed = feed;
		fe.iterator = this;
		return fe;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Remove operation is not supported!");
	}

	@Override
	public void close() {
		if (contentIterator instanceof Closeable)
			try {
				((Closeable)contentIterator).close();
			} catch (IOException e) {
				log.error("Failed to close content iterator: "+contentIterator);
			}
	}

	public EventRecipientClass getRecipient() {
		return recipient;
	}

}
