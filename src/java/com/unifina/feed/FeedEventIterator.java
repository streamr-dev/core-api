package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A helper class to apply a static recipient and iterator to FeedEvents,
 * whose content is pulled from a separate content iterator.
 *
 * This iterator assumes that each item returned by the content iterator
 * produces one FeedEvent.
 * @author Henri
 */
public class FeedEventIterator<MessageClass extends AbstractStreamrMessage, EventRecipientClass extends IEventRecipient>
		implements Iterator<FeedEvent<MessageClass, EventRecipientClass>>, Closeable {

	private Iterator<MessageClass> contentIterator;
	private EventRecipientClass recipient;
	private AbstractHistoricalFeed feed;
	
	private final Logger log = Logger.getLogger(FeedEventIterator.class);

	private int currentEventsIndex = 0;
	private FeedEvent[] currentEvents = null;
	
	public FeedEventIterator(Iterator<MessageClass> contentIterator, AbstractHistoricalFeed feed, EventRecipientClass recipient) {
		this.contentIterator = contentIterator;
		this.recipient = recipient;
		this.feed = feed;
	}
	
	@Override
	public boolean hasNext() {
		return contentIterator.hasNext() || currentEvents != null && currentEventsIndex < currentEvents.length;
	}

	@Override
	public FeedEvent<MessageClass, EventRecipientClass> next() {
		while (currentEvents==null || currentEvents.length==0 || currentEventsIndex >= currentEvents.length) {
			MessageClass content = contentIterator.next();

			if (content == null) {
				throw new NoSuchElementException("No more content from iterator "+contentIterator);
			} else {
				currentEvents = content.toFeedEvents(recipient);
				currentEventsIndex = 0;
			}
		}

		FeedEvent<MessageClass, EventRecipientClass> fe = currentEvents[currentEventsIndex++];
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
