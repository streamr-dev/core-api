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
 * whose content messages (AbstractStreamrMessages) are pulled from an underlying
 * message iterator.
 *
 * If an AbstractStreamrMessage produces multiple FeedEvents, they are returned before
 * the message iterator is consulted for more messages.
 * @author Henri
 */
public class FeedEventIterator<MessageClass extends AbstractStreamrMessage, EventRecipientClass extends IEventRecipient>
		implements Iterator<FeedEvent<MessageClass, EventRecipientClass>>, Closeable {

	private Iterator<MessageClass> messageIterator;
	private EventRecipientClass recipient;
	private AbstractHistoricalFeed feed;
	
	private final Logger log = Logger.getLogger(FeedEventIterator.class);

	private int currentEventsIndex = 0;
	private FeedEvent[] currentEvents = null;
	
	public FeedEventIterator(Iterator<MessageClass> messageIterator, AbstractHistoricalFeed feed, EventRecipientClass recipient) {
		this.messageIterator = messageIterator;
		this.recipient = recipient;
		this.feed = feed;
	}
	
	@Override
	public boolean hasNext() {
		return messageIterator.hasNext() || currentEvents != null && currentEventsIndex < currentEvents.length;
	}

	@Override
	public FeedEvent<MessageClass, EventRecipientClass> next() {
		while (currentEvents==null || currentEvents.length==0 || currentEventsIndex >= currentEvents.length) {
			MessageClass message = messageIterator.next();

			if (message == null) {
				throw new NoSuchElementException("No more messages from iterator "+ messageIterator);
			} else {
				currentEvents = message.toFeedEvents(recipient);
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
		if (messageIterator instanceof Closeable)
			try {
				((Closeable) messageIterator).close();
			} catch (IOException e) {
				log.error("Failed to close message iterator: "+ messageIterator);
			}
	}

	public EventRecipientClass getRecipient() {
		return recipient;
	}

}
