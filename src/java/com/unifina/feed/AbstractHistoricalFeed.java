package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Implements a basic interface for iterating over FeedEvents from a historical feed. The FeedEvent have
 * a content of type MessageClass.
 */
public abstract class AbstractHistoricalFeed<ModuleClass, MessageClass extends ITimestamped, KeyClass, EventRecipientClass extends IEventRecipient>
		extends AbstractFeed<ModuleClass, MessageClass, KeyClass, EventRecipientClass>
		implements Iterator<FeedEvent<MessageClass, EventRecipientClass>> {
	
	/**
	 * The queue orders FeedEvents from multiple streams in their natural order (timestamp, queue insertion order)
	 */
	protected PriorityQueue<FeedEvent<MessageClass, EventRecipientClass>> queue = new PriorityQueue<>();
	private HashSet<EventRecipientClass> iteratorCreatedFor = new HashSet<>();
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
		for (EventRecipientClass recipient : eventRecipients) {
			Iterator<FeedEvent<MessageClass, EventRecipientClass>> iterator = createIteratorFor(recipient);
			if (iterator!=null && iterator.hasNext()) {
				FeedEvent<MessageClass, EventRecipientClass> event = iterator.next();
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
			if (event.iterator != null) {
				event.iterator.close();
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
	public FeedEvent<MessageClass, EventRecipientClass> next() {
		FeedEvent<MessageClass, EventRecipientClass> event = queue.poll();
		
		if (event==null)
			throw new NoSuchElementException();
		
		// From the same iterator, get the next event
		Iterator<FeedEvent<MessageClass, EventRecipientClass>> iterator = event.iterator;

		// If the next event exists, add it to the queue
		if (iterator!=null && iterator.hasNext()) {
			queue.add(iterator.next());
		}
		
		return event;
	}
	
	@Override
	public void remove() {
		throw new RuntimeException("Remove operation is not supported!");
	}

	/**
	 * Should return a list of date pairs, over which the calculation can be distributed (units).
	 * Date[0] represents unit start datetime and date[1] represents unit end datetime.

	 * @param beginDate Earliest unit start datetime than can be returned
	 * @param endDate Latest unit end datetime that can be returned
	 * @return
	 */
	public abstract List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception;

	/**
	 * Wraps the iterator creation in a check that ensures an iterator is created only once per recipient.
     */
	private Iterator<FeedEvent<MessageClass, EventRecipientClass>> createIteratorFor(EventRecipientClass recipient) {
		if (iteratorCreatedFor.contains(recipient)) {
			throw new IllegalStateException("Iterator has already been created for recipient "+recipient);
		}

		iteratorCreatedFor.add(recipient);
		return iterator(recipient);
	}

	/**
	 * Returns an Iterator for FeedEvents required by the given event recipient.
	 * It is guaranteed that this method only gets called once per each recipient.
	 */
	protected abstract Iterator<FeedEvent<MessageClass, EventRecipientClass>> iterator(EventRecipientClass recipient);

}
