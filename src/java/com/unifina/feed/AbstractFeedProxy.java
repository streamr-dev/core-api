package com.unifina.feed;

import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventQueue;
import com.unifina.data.IEventRecipient;
import com.unifina.data.IFeed;
import com.unifina.utils.Globals;


/**
 * Acts as a feed instance in a realtime situation. Really gets its messages
 * from an underlying singleton hub, an AbstractMessageHub. For every session
 * there is one feed proxy holding the session's subscriptions. There can be 
 * only one actual feed implementation, which is the hub.
 * 
 * This class receives preprocessed messages (of type T) from the hub,
 * filters them using the collection of subscribed objects and creates
 * FeedEvents from the preprocessed message, finally pushing them into
 * the event queue.
 * 
 * @author Henri
 *
 */
public abstract class AbstractFeedProxy<T> extends Thread implements IFeed, ICatchupFeed {
	
	// The FeedEvents are pushed
	private IEventQueue eventQueue;
	
	private int expected = 0;
//	private int catchupCounter = 0;
	
	protected TimeZone tz;

	protected HashMap<Object,Object> subscriptionsByKey = new HashMap<>();
	protected HashMap<Object,IEventRecipient> eventRecipientsByKey = new HashMap<>();
	
	protected List<Class> validSubscribeTypes;
	protected AbstractMessageHub<T> hub;
	
	private Catchup catchup = null;
	enum CatchupState { CATCHUP, CATCHUP_UNSYNC_READY, CATCHUP_READY };
	private CatchupState catchupState = CatchupState.CATCHUP; // start in catchup state
	
	private ConcurrentLinkedQueue<T> realtimeWaitQueue = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<Integer> realtimeWaitQueueCounter = new ConcurrentLinkedQueue<>();
	
	private static final Logger log = Logger.getLogger(AbstractFeedProxy.class);
	private static final boolean checkEventAge = true;
	
	private Integer firstWaitQueue = null;
	private Integer firstRealQueue = null;
	
	public AbstractFeedProxy(Globals globals) {
		hub = getMessageHub();
		validSubscribeTypes = getValidSubscribeTypes();
	}
	
	protected abstract AbstractMessageHub<T> getMessageHub();
	
	/**
	 * This should return a list of classes that are valid parameters
	 * for a call to subscribe(Object)
	 * @return
	 */
	protected abstract List<Class> getValidSubscribeTypes();
	
	/**
	 * Returns an Object that will be used to lookup the subscriber
	 * and the event recipient.
	 * @param subscriber
	 * @return
	 */
	protected abstract Object getSubscriptionKey(Object subscriber);
	
	/**
	 * Creates an IEventRecipient that should be set on FeedEvents 
	 * created for this subscription. 
	 * @param sub
	 * @return
	 */
	protected abstract IEventRecipient createEventRecipientForSubscription(Object sub);
	
	public boolean isSubscribed(Object item) {
		return canSubscribe(item) && subscriptionsByKey.containsKey(getSubscriptionKey(item));
	}
	
	public boolean canSubscribe(Object item) {
		boolean valid = false;
		for (Class c : validSubscribeTypes) {
			if (c.isAssignableFrom(item.getClass())) {
				valid = true;
				break;
			}
		}
		return valid;
	}
	
	/**
	 * Creates a new subscription on this feed and creates an event handler for
	 * this subscription. Returns true if the subscription was successful,
	 * false if the object was already subscribed. Throws an IllegalArgumentException
	 * if the subscription is of the wrong type.
	 */
	public boolean subscribe(Object sub) {
		if (!canSubscribe(sub)) {
			throw new IllegalArgumentException("This feed can only subscribe items of the following classes: "+validSubscribeTypes);
		}
		
		Object key = getSubscriptionKey(sub);
		
		if (!isSubscribed(sub)) {
			subscriptionsByKey.put(key,sub);
			eventRecipientsByKey.put(key, createEventRecipientForSubscription(sub));
		}
		else {
			Object subscribed = subscriptionsByKey.get(key); 
			// Check that the subscribed object is the same object!
			if (subscribed!=sub)
				throw new IllegalStateException("Feed subscriptions with the same key are not same objects! Subscribed: "+subscribed+", New: "+sub);
			else return false;
		}
		
		return true;
	}
	
	/**
	 * This method is called by the hub distribute messages to session-specific proxies.
	 * It processes the message and adds it to the event queue - or if a catchup is in
	 * progress, it adds them to the wait queue.
	 * @param counter
	 * @param msg
	 */
	public void receive(int counter, T msg) {
		// If still waiting for catchup to end, produce to the wait queue
		if (catchupState==CatchupState.CATCHUP) {
			realtimeWaitQueue.add(msg);
			realtimeWaitQueueCounter.add(counter);
//			log.info("receive: Message added to wait queue: "+counter);
		}
		else {
			// ProcessAndQueue a message with the correct counter
			if (counter==expected) {
				processAndQueue(counter, msg, checkEventAge);
			}
			// If there is a gap, try to handle it
			else if (counter > expected) {
				// If the gap was handled successfully, processAndQueue the current message
				if (handleGap(counter)) {
					processAndQueue(counter, msg, checkEventAge);
				}
				// Else place the message into the wait queue for future processing
				else {
					log.warn("receive: Failed to handle the gap, placing msg into wait queue: "+counter+", expected: "+expected);
					realtimeWaitQueue.add(msg);
					realtimeWaitQueueCounter.add(counter);
				}
			}
			// Discard old messages
			else if (counter < expected) {
				log.warn("Discarding duplicate message: "+counter+", expected: "+expected);
				return;
			}
		}
	}
	
	/**
	 * This method checks the wait queue and/or catchup stream for missing
	 * messages. When this message returns, expected==counter must be true
	 * or else an Exception must be thrown.
	 * @param counterTarget
	 * @return true if the gap was fixed, false if not
	 */
	private boolean handleGap(int counterTarget) {
		log.info("handleGap: Trying to process events from the wait queue. Counter: "+counterTarget+", expected: "+expected+".");
		log.info("Wait queue contains "+realtimeWaitQueue.size()+" messages, first one has counter "+realtimeWaitQueueCounter.peek());
		
		while (expected < counterTarget) {
			
			// Purge any already-processed messages from the wait queue
			while (!realtimeWaitQueue.isEmpty() && realtimeWaitQueueCounter.peek()<expected) {
				Integer waitCounter = realtimeWaitQueueCounter.poll();
				realtimeWaitQueue.poll();
				log.warn("handleGap: old message purged from wait queue: "+waitCounter+", expected: "+expected);
			}
			
			// Is the correct counter in the wait queue?
			if (!realtimeWaitQueue.isEmpty() && realtimeWaitQueueCounter.peek()==expected) {
				Integer waitCounter = realtimeWaitQueueCounter.poll();
				T waitMsg = realtimeWaitQueue.poll();
				processAndQueue(waitCounter, waitMsg, false);
				log.info("handleGap: Message processed from wait queue: "+waitCounter);
			}
			
			// If not, try to find the expected message in catchup
			else {
				// If the catchup lags behind, fast-forward it
				while (catchup.getNextCounter()<expected) {
					Object next = catchup.getNext();
					if (next==null) {
						log.warn("Catchup stops early at "+catchup.getNextCounter()+", expected: "+expected+", wait queue head: "+realtimeWaitQueueCounter.peek());
						return false;
					}
				}

				// Is the expected message found in catchup?
				int counter = catchup.getNextCounter();
				Object next = catchup.getNext();
				if (next==null) {
					log.warn("Catchup does not contain expected message: "+expected+", wait queue head: "+realtimeWaitQueueCounter.peek());
					return false;
				}
				else {
					log.info("handleGap: Message processed from catchup: "+counter);
					T msg = hub.preprocess(next);
					processAndQueue(counter, msg, false);
				}
			}
		}

		// Catchup complete
//		log.info("The wait queue has been depleted, setting catchup state to ready!");
//		catchupState = CatchupState.CATCHUP_READY;
		return true;
	}
	
	private void processAndQueue(int counter, T msg, boolean checkAge) {
		if (counter!=expected)
			throw new IllegalArgumentException("Tried to process messages in invalid order! Counter: "+counter+", expected: "+expected);
		else {
			expected++;
			FeedEvent[] events = process(msg);

			// TODO: remove debug
			if (firstRealQueue==null && checkAge) {
				log.info("First real time message: "+counter+". Events: "+events);
				firstRealQueue = counter;
			}

			if (events!=null) {
				// Warn about old events
				if (checkAge && events.length>0) {
					long age = System.currentTimeMillis() - events[0].timestamp.getTime();
					if (age>1000L)
						log.warn("Event age "+age+": "+events[0]);
				}

				for (FeedEvent event : events)
					eventQueue.enqueue(event);
			}
		}
	}
	
	/**
	 * This method is guaranteed to be called in correct order without gaps.
	 * It should convert the feed message to FeedEvent(s).
	 */
	protected abstract FeedEvent[] process(T msg);
	
	
	@Override
	public FeedEvent[] getNextEvents() {
		if (catchup==null)
			throw new IllegalStateException("Catchup is not started!");
		
		Object line;
		T msg;
		FeedEvent[] result = null;
		
		while(result==null) {
			int catchupCounter = catchup.getNextCounter();
			line = catchup.getNext();

			if (line==null) {
//				log.info("getNextEvents: Catchup is ready! Message "+catchupCounter+" does not exist. Expected: "+expected);
				return null;
			}
			
			msg = hub.preprocess(line);
			
			if (catchupCounter!=expected)
				throw new IllegalStateException("Gap in catchup! Counter: "+catchupCounter+", expected: "+expected);
			else {
				expected++;
				result = process(msg);
			}

			// Remove overlap in the wait queue
			Integer cc = realtimeWaitQueueCounter.peek();
			if (cc!=null && cc==catchupCounter) {
//				log.info("getNextEvents: Removing duplicate from wait queue: "+cc);
				realtimeWaitQueueCounter.poll();
				realtimeWaitQueue.poll();
			}
			
		}
		
		return result;
	}
	
	@Override
	public void startFeed() throws Exception {
		getMessageHub().addProxy(this);
	}
	
	@Override
	public void stopFeed() throws Exception {
		getMessageHub().removeProxy(this);
		log.info("Unsubscribed from hub: "+this);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(this.getClass());
	}
	

	@Override
	public void setTimeZone(TimeZone tz) {
		this.tz = tz;
	}
	
	@Override
	public void setEventQueue(IEventQueue queue) {
		this.eventQueue = queue;
	}
	
	@Override
	public boolean startCatchup() {
		log.info("Starting catchup");
		catchup = getMessageHub().startCatchup(this);
		
		if (catchup==null) {
			return false;
		}
		else {
//			catchupState = CatchupState.CATCHUP;
			return true;
		}
	}

	@Override
	public void endCatchup() {
		if (catchup==null)
			throw new IllegalStateException("Catchup is not started!");
		
		log.info("endCatchup called");
		catchupState = CatchupState.CATCHUP_UNSYNC_READY;
	}

	public void sessionBroken() {
		// TODO Auto-generated method stub
		
	}

	public void sessionRestored() {
		// TODO Auto-generated method stub
		
	}

	public void sessionTerminated() {
		// TODO Auto-generated method stub
		
	}
	
}

