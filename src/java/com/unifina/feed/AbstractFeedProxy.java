package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;


/**
 * Acts as a feed instance in a realtime situation. Really gets its messages
 * from an underlying singleton hub, an AbstractMessageHub. For every session
 * there is one feed proxy holding the session's subscriptions. There can be 
 * only one actual feed implementation, which is the hub.
 * 
 * This class receives preprocessed messages (of type MessageClass) from the hub,
 * filters them using the collection of subscribed objects and creates
 * FeedEvents from the preprocessed message, finally pushing them into
 * the event queue.
 * 
 * @author Henri
 *
 */
public abstract class AbstractFeedProxy<ModuleClass, RawMessageClass, MessageClass extends ITimestamped, KeyClass, EventRecipientClass extends IEventRecipient>
		extends AbstractFeed<ModuleClass, MessageClass, KeyClass, EventRecipientClass>
		implements MessageRecipient<RawMessageClass, KeyClass> {

	private static final Logger log = Logger.getLogger(AbstractFeedProxy.class);

	private final MessageHub<RawMessageClass, MessageClass, KeyClass> hub;

	private Long firstRealQueue = null;
	private int expected = 0;

	public AbstractFeedProxy(Globals globals, Feed domainObject) {
		super(globals, domainObject);
		hub = getMessageHub();
	}

	@Override
	public boolean subscribe(ModuleClass subscriber) {
		for (KeyClass key : keyProvider.getSubscriberKeys(subscriber)) {
			hub.subscribe(key, this);
		}
		return super.subscribe(subscriber);
	}
	
	/**
	 * This method is called by the hub to distribute messages to session-specific proxies.
	 * It processes the message and adds it to the event queue.
	 */
	@Override
	public void receive(Message parsedMsg) {
		MessageClass msg = (MessageClass) parsedMsg.message;
		// ProcessAndQueue a message with the correct counter
		if (parsedMsg.counter == expected || !parsedMsg.checkCounter) {
			processAndQueue(parsedMsg.checkCounter ? parsedMsg.counter : expected, msg, true);
		} else if (parsedMsg.counter > expected) { // If there is a gap, try to handle it
			// If the gap was handled successfully, processAndQueue the current message
			processAndQueue(parsedMsg.counter, msg, true);
		} else { // Discard old messages
			log.warn("Discarding duplicate message: " + parsedMsg.counter + ", expected: " + expected);
		}
	}

	private MessageHub<RawMessageClass, MessageClass, KeyClass> getMessageHub() {
		try {
			return FeedFactory.getInstance(domainObject, Holders.getGrailsApplication().getConfig());
		} catch (InstantiationException | ClassNotFoundException
			| NoSuchMethodException | InvocationTargetException
			| IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void processAndQueue(long counter, MessageClass msg, boolean checkAge) {
		if (counter != expected) {
			throw new IllegalArgumentException("Tried to process messages in invalid order! Counter: " + counter + ", expected: " + expected);
		} else {
			expected++;
			FeedEvent<MessageClass, EventRecipientClass>[] events = process(msg);

			if (firstRealQueue == null && checkAge) {
				firstRealQueue = counter;
			}

			if (events != null) {
				// Warn about old events
				if (checkAge && events.length > 0) {
					long age = System.currentTimeMillis() - events[0].timestamp.getTime();
					if (age > 1000L) {
						log.warn("Event age " + age + ": " + events[0]);
					}
				}

				for (FeedEvent event : events) {
					eventQueue.enqueue(event);
				}
			}
		}
	}
	
	/**
	 * This method is guaranteed to be called in correct order without gaps.
	 * It should convert the feed message to FeedEvent(s).
	 */
	protected abstract FeedEvent<MessageClass, EventRecipientClass>[] process(MessageClass msg);
	
	@Override
	public void startFeed() throws Exception {
		hub.addRecipient(this);
	}
	
	@Override
	public void stopFeed() throws Exception {
		for (ModuleClass subscriber : subscribers) {
			for (KeyClass key : keyProvider.getSubscriberKeys(subscriber)) {
				hub.unsubscribe(key, this);
			}
		}
		
		hub.removeRecipient(this);
		log.info("Unsubscribed from hub: "+this);
	}

	public MessageHub<RawMessageClass, MessageClass, KeyClass> getHub() {
		return hub;
	}

	@Override
	public void sessionBroken() {}

	@Override
	public void sessionRestored() {}

	@Override
	public void sessionTerminated() {}

	@Override
	public int getReceivePriority() {
		return 0;
	}
}
