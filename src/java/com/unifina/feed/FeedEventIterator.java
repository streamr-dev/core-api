package com.unifina.feed;

import com.unifina.data.Event;
import com.unifina.data.HistoricalEvent;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import com.streamr.client.protocol.message_layer.ITimestamped;

/**
 * A helper class to apply a static recipient and iterator to FeedEvents,
 * whose content is pulled from a separate content iterator.
 */
public class FeedEventIterator<MessageClass extends ITimestamped>
		implements Iterator<Event<MessageClass>>, Closeable {

	private Iterator<MessageClass> contentIterator;
	private AbstractEventRecipient recipient;
	private long counter = 0;

	private final Logger log = Logger.getLogger(FeedEventIterator.class);

	public FeedEventIterator(Iterator<MessageClass> contentIterator, AbstractEventRecipient recipient) {
		this.contentIterator = contentIterator;
		this.recipient = recipient;
	}

	@Override
	public boolean hasNext() {
		return contentIterator.hasNext();
	}

	@Override
	public Event<MessageClass> next() {
		MessageClass content = contentIterator.next();
		if (content==null)
			return null;

		Event<MessageClass> fe = new HistoricalEvent<>(
			content,
			content.getTimestampAsDate(),
			counter++,
			recipient,
			this
		);
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

	public AbstractEventRecipient getRecipient() {
		return recipient;
	}

}
