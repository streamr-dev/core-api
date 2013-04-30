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
	private int catchupCounter = 0;
	
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
			if (catchupState==CatchupState.CATCHUP) {
				// TODO: remove debug
				if (firstWaitQueue==null) {
					log.info("First message added to wait queue: "+counter);
					firstWaitQueue = counter;
				}
				
				// Produce to the wait queue
				realtimeWaitQueue.add(msg);
				realtimeWaitQueueCounter.add(counter);
			}
			// Normal state, when there is no catching up going on. Avoid synchronizing this for speed
			else {
				FeedEvent[] events = process(counter, msg);
				
				// TODO: remove debug
				if (firstRealQueue==null) {
					log.info("First message added to event queue: "+counter+". Events: "+events);
					firstRealQueue = counter;
				}
				
				if (events!=null) {
					if (checkEventAge && events.length>0) {
						long age = System.currentTimeMillis() - events[0].timestamp.getTime();
						if (age>1000L)
							log.warn("Event age "+age+": "+events[0]);
					}
					
					for (FeedEvent event : events)
						eventQueue.enqueue(event);
				}
			}
	}
	
	private boolean ensureCorrectCounter(int counter) {
		if (counter<expected) {
			log.warn("Discarding duplicate message: "+counter+", expected: "+expected);
			return false;
		}		
		// Check if messages can be processed from the wait queue
		else if (counter!=expected) {
			log.info("Counter less than expected, trying to process events from the wait queue. Counter: "+counter+", expected: "+expected+".");
			log.info("Wait queue contains "+realtimeWaitQueue.size()+" messages, first one has counter "+realtimeWaitQueueCounter.peek());
			
			while (counter > expected) {
				T cup = realtimeWaitQueue.poll();

				if (cup==null) {
					throw new IllegalStateException("Wait queue empty while waiting for correct counter! Expected: "+expected+", Counter: "+counter);
				}

				int cupCounter = realtimeWaitQueueCounter.poll();

				if (cupCounter!=expected)
					throw new IllegalStateException("Expected counter not found in catchup! Expected: "+expected+", Catchup: "+cupCounter);
				else {
					FeedEvent[] events = process(cupCounter, cup);
					if (events!=null)
						for (FeedEvent event : events)
							eventQueue.enqueue(event);
				}
			}

			// Catchup complete
			log.info("The wait queue has been depleted, setting catchup state to ready!");
			catchupState = CatchupState.CATCHUP_READY;
			catchup = null;
		}
		
		expected++;
		return true;
	}
	
	private FeedEvent[] process(int counter, T msg) {
		if (ensureCorrectCounter(counter)) {
			return doProcess(msg);
		}
		else return null;
	}
	
	/**
	 * This method is guaranteed to be called in correct order without gaps 
	 */
	protected abstract FeedEvent[] doProcess(T msg);
	
	
	@Override
	public FeedEvent[] getNextEvents() {
		if (catchup==null)
			throw new IllegalStateException("Catchup is not started!");
		
		Object line;
		T msg;
		FeedEvent[] result = null;
		
		while(result==null) {
			line = catchup.getNext();

			if (line==null) {
				System.out.println("Catchup is ready! Read "+catchupCounter+" messages.");
				return null;
			}
			
			msg = hub.preprocess(line);
			result = process(catchupCounter, msg);

			// Remove overlap in the catchupQueue
			Integer cc = realtimeWaitQueueCounter.peek();
			if (cc!=null && cc==catchupCounter) {
//				System.out.println("Removing duplicate from catchupQueue: "+cc);
				realtimeWaitQueueCounter.poll();
				realtimeWaitQueue.poll();
			}
			
			catchupCounter++;
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

