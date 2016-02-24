package com.unifina.feed;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;

/**
 * A helper class to apply a static recipient and iterator to FeedEvents,
 * whose content is pulled from a separate content iterator.
 * @author Henri
 */
public class FeedEventIterator implements Iterator<FeedEvent>, Closeable {

	private Iterator<? extends Object> contentIterator;
	private IEventRecipient recipient;
	private AbstractHistoricalFeed feed;
	
	private final Logger log = Logger.getLogger(FeedEventIterator.class);
	
	public FeedEventIterator(Iterator<? extends Object> contentIterator, AbstractHistoricalFeed feed, IEventRecipient recipient) {
		this.contentIterator = contentIterator;
		this.recipient = recipient;
		this.feed = feed;
	}
	
	@Override
	public boolean hasNext() {
		return contentIterator.hasNext();
	}

	@Override
	public FeedEvent next() {
		Object content = contentIterator.next();
		if (content==null)
			return null;
		
		FeedEvent fe = new FeedEvent(content, feed.getTimestamp(content, contentIterator), recipient);
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

	public IEventRecipient getRecipient() {
		return recipient;
	}
}
