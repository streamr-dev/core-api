package com.unifina.feed;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.data.IBacktestFeed;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

/**
 * Implements a basic interface for getting FeedEvents from a historical feed via getNext()
 */
public abstract class AbstractHistoricalFeed extends AbstractFeed implements Iterator<FeedEvent> {
	
	/**
	 * The queue orders FeedEvents from multiple streams in their natural order (timestamp, queue insertion order)
	 */
	protected PriorityQueue<FeedEvent> queue = new PriorityQueue<>();
	protected boolean started = false;
	
	private static final Logger log = Logger.getLogger(AbstractHistoricalFeed.class);
	
	public AbstractHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	public void startFeed() throws Exception {
		started = true;
		
		log.debug("Starting feed with event recipients by key: "+eventRecipientsByKey);

		// For each recipient get an input stream and place the first event in a PriorityQueue
		for (IEventRecipient recipient : eventRecipients) {
			FeedEventIterator iterator = getNextIterator(recipient);
			if (iterator!=null) {
				FeedEvent event = iterator.next();
				if (event!=null)
					queue.add(event);
			}
		}
		
		log.debug("Starting contents of event queue: "+queue);
	}
	
	@Override
	public void stopFeed() throws Exception {
		started = false;
		
		// Close all the remaining unclosed sources
		for (FeedEvent event : queue) {
			if (event.iterator instanceof Closeable) {
				((Closeable)event.iterator).close();
			}
		}
	}
	
	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}
	
    /**
     * Returns the next FeedEvent in the iteration.
     *
     * @return the next FeedEvent in the iteration
     * @throws NoSuchElementException if the iteration has no more FeedEvents
     */
	@Override
	public FeedEvent next() {
		FeedEvent event = queue.poll();
		
		if (event==null)
			throw new NoSuchElementException();
		
		// From the same stream, get the next event
		FeedEventIterator iterator = (FeedEventIterator) event.iterator; // TODO: avoid cast?
		FeedEvent nxt = iterator.next();
		
		// No next event, try to get the next stream piece and the next event from there
		while (nxt==null && started) {
			try {
				// Close the old feed reader
				if (iterator instanceof Closeable)
					((Closeable)iterator).close();
				
				iterator = getNextIterator(iterator.getRecipient());
			} catch (IOException e) {
				throw new RuntimeException("IOException thrown while getting next event iterator!");
			}
			
			// If the next stream was found, try to get an event
			if (iterator!=null)
				nxt = iterator.next();
			// If no next stream, the we're done for this stream
			else break;
		}
		
		// If the next event exists, add it to the queue
		if (nxt!=null) {
			queue.add(nxt);
		}
		
		return event;
	}
	
	@Override
	public void remove() {
		throw new RuntimeException("Remove operation is not supported!");
	}

	/**
	 * Should return a list of date pairs, where date[0] represents unit
	 * start datetime and date[1] represents unit end datetime.

	 * @param beginDate Earliest unit start datetime than can be returned
	 * @param endDate Latest unit end datetime that can be returned
	 * @return
	 */
	public abstract List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception;
	
	/**
	 * Returns the iterator for the next unit
	 * @param recipient
	 * @return
	 */
	protected abstract FeedEventIterator getNextIterator(IEventRecipient recipient) throws IOException;
	
	/**
	 * Extracts a Date from the specified event content.
	 * @param eventContent the content return by the content iterator, for which a timestamp is needed
	 * @param contentIterator the contentIterator that produced the eventContent
	 * @return
	 */
	protected abstract Date getTimestamp(Object eventContent, Iterator<? extends Object> contentIterator);
	
}
