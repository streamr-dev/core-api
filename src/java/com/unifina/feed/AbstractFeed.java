package com.unifina.feed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.unifina.data.IEventQueue;
import com.unifina.data.IEventRecipient;
import com.unifina.data.IFeed;
import com.unifina.utils.Globals;

public abstract class AbstractFeed implements IFeed {

	protected IEventQueue eventQueue;
	
//	protected HashMap<Object,List<Object>> subscriptionsByKey = new HashMap<>();
	protected Set<Object> subscribers = new HashSet<Object>();
	protected HashMap<Object,IEventRecipient> eventRecipientsByKey = new HashMap<>();
	protected List<Class> validSubscribeTypes;
	
	protected Globals globals;
	protected TimeZone timeZone;
	
	public AbstractFeed(Globals globals) {
		this.globals = globals;
		validSubscribeTypes = getValidSubscriberClasses();
	}
	
	
	/**
	 * This should return a list of classes that are valid parameters
	 * for a call to subscribe(Object)
	 * @return
	 */
	protected abstract List<Class> getValidSubscriberClasses();
	
	/**
	 * Returns an Object that will be used to lookup the subscriber
	 * and the event recipient.
	 * @param subscriber
	 * @return
	 */
	protected abstract Object getEventRecipientKey(Object subscriber);
	
	/**
	 * Creates an IEventRecipient that should be set on FeedEvents 
	 * created for this subscriber. 
	 * @param subscriber
	 * @return
	 */
	protected abstract IEventRecipient createEventRecipient(Object subscriber);
	
	public boolean isSubscribed(Object item) {
		return subscribers.contains(item);
//		return canSubscribe(item) && (!subscriptionsByKey.containsKey(getSubscriptionKey(item)) || subscriptionsByKey.get(getSubscriptionKey(item))!=item);
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
	public boolean subscribe(Object subscriber) {
		if (!canSubscribe(subscriber)) {
			throw new IllegalArgumentException("This feed can only subscribe items of the following classes: "+validSubscribeTypes);
		}

		// Don't subscribe the same object twice
		if (!isSubscribed(subscriber)) {

			subscribers.add(subscriber);

			// Create and register the event recipient for this subscription if it doesn't already exist
			IEventRecipient recipient;
			Object key = getEventRecipientKey(subscriber);
			if (!eventRecipientsByKey.containsKey(key)) {
				recipient = createEventRecipient(subscriber);
				eventRecipientsByKey.put(key, recipient);
				globals.getDataSource().register(recipient);
			}
			else recipient = eventRecipientsByKey.get(key);
			
			// Register the subscription with the event recipient
			if (recipient instanceof AbstractEventRecipient) {
				((AbstractEventRecipient)recipient).register(subscriber);
			}
			
		}
		return true;
	}
	
//	@Override
//	public IEventRecipient getEventRecipient(Object sub) {
//		Object key = getSubscriptionKey(sub);
//		return eventRecipientsByKey.get(key);
//	}

	@Override
	public void setEventQueue(IEventQueue queue) {
		this.eventQueue = queue;
	}


	@Override
	public void setTimeZone(TimeZone tz) {
		this.timeZone = tz;
	}
	
}
