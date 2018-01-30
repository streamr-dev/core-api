package com.unifina.feed;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import com.unifina.data.IEventRecipient;
import com.unifina.data.IStreamRequirement;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.utils.Globals;

/**
 * An AbstractFeed is responsible for starting and stopping an event stream
 *   as well as matching the events and their IEventRecipients.
 * @param <ModuleClass> Describes the kind of subscribers this Feed can take. The subscribers are usually modules that require a certain Stream, and you should use the most general type you can (often the IStreamRequirement interface).
 * @param <MessageClass> The kind of messages this Feed produces and sends to the relevant IEventRecipients. Must implement ITimestamped.
 * @param <KeyClass> The key is also used to subscribe the MessageSource with MessageSource#subscribe(key). The key also binds subscribers and messages together.
 * @author Henri
 */
public abstract class AbstractFeed<ModuleClass, MessageClass extends ITimestamped, KeyClass, EventRecipientClass extends IEventRecipient> {

	protected DataSourceEventQueue eventQueue;
	
	protected Set<ModuleClass> subscribers = new HashSet<>();
	
	protected ArrayList<EventRecipientClass> eventRecipients = new ArrayList<>();
	protected HashMap<KeyClass, EventRecipientClass> eventRecipientsByKey = new HashMap<>();
	
	protected Globals globals;
	protected TimeZone timeZone;

	protected Feed domainObject;
	protected AbstractKeyProvider<ModuleClass, MessageClass, KeyClass> keyProvider;
	
	public AbstractFeed(Globals globals, Feed domainObject) {
		this.globals = globals;
		this.domainObject = domainObject;
		keyProvider = createKeyProvider();
	}

	protected AbstractKeyProvider<ModuleClass, MessageClass, KeyClass> createKeyProvider() {
		try {
			Class keyProviderClass = this.getClass().getClassLoader().loadClass(domainObject.getKeyProviderClass());
			Constructor constructor = keyProviderClass.getConstructor(Globals.class, Feed.class);
			return (AbstractKeyProvider) constructor.newInstance(globals, domainObject);
		} catch (Exception e) {
			throw new RuntimeException("Could not create key provider of class "+domainObject.getKeyProviderClass(),e);
		}
	}
	
	/**
	 * Creates an EventRecipientClass that should be set on FeedEvents
	 * created for this subscriber. 
	 * @param subscriber
	 * @return
	 */
	protected EventRecipientClass createEventRecipient(ModuleClass subscriber) {
		try {
			Class eventRecipientClass = this.getClass().getClassLoader().loadClass(domainObject.getEventRecipientClass());

			// Construction of StreamEventRecipients (represented by a Stream object in the database)
			if (subscriber instanceof IStreamRequirement && StreamEventRecipient.class.isAssignableFrom(eventRecipientClass)) {
				Constructor constructor = eventRecipientClass.getConstructor(Globals.class, Stream.class, Set.class);
				return (EventRecipientClass) constructor.newInstance(globals, ((IStreamRequirement)subscriber).getStream(), ((IStreamRequirement)subscriber).getPartitions());
			}
			// Construction of other IEventRecipients (legacy/hardwired)
			else {
				Constructor constructor = eventRecipientClass.getConstructor(Globals.class);
				return (EventRecipientClass) constructor.newInstance(globals);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not create an EventRecipient of class "+domainObject.getEventRecipientClass()+" for subscriber "+subscriber,e);
		}
	}

	public EventRecipientClass getEventRecipientByKey(KeyClass key) {
		return eventRecipientsByKey.get(key);
	}

	public EventRecipientClass getEventRecipientForMessage(MessageClass message) {
		return eventRecipientsByKey.get(keyProvider.getMessageKey(message));
	}
	
	public boolean isSubscribed(ModuleClass subscriber) {
		return subscribers.contains(subscriber);
	}	
	
	/**
	 * Creates a new subscription on this feed and creates an event handler for
	 * this subscription. Returns true if the subscription was successful,
	 * false if the object was already subscribed. Throws an IllegalArgumentException
	 * if the subscription is of the wrong type.
	 */
	public boolean subscribe(ModuleClass subscriber) {

		// Don't subscribe the same object twice
		if (!isSubscribed(subscriber)) {

			subscribers.add(subscriber);

			// Create and register the event recipient for this subscription if it doesn't already exist
			for (KeyClass key : keyProvider.getSubscriberKeys(subscriber)) {
				EventRecipientClass recipient = getEventRecipientByKey(key);

				if (recipient == null) {
					recipient = createEventRecipient(subscriber);
					eventRecipients.add(recipient);
					eventRecipientsByKey.put(key, recipient);
					globals.getDataSource().register(recipient);
				}

				// Register the subscription with the event recipient
				if (recipient instanceof AbstractEventRecipient) {
					((AbstractEventRecipient<ModuleClass, MessageClass>)recipient).register(subscriber);
				}
			}
			
		}
		return true;
	}

	public void setEventQueue(DataSourceEventQueue queue) {
		this.eventQueue = queue;
	}

	public void setTimeZone(TimeZone tz) {
		this.timeZone = tz;
	}
	
	public abstract void startFeed() throws Exception;
	public abstract void stopFeed() throws Exception;
	
}
