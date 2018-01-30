package com.unifina.feed;

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.unifina.data.IEventRecipient;
import grails.util.Holders;
import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;


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
		implements MessageRecipient<RawMessageClass, KeyClass>, ICatchupFeed {

	enum CatchupState {
		CATCHUP,
		CATCHUP_UNSYNC_READY
	}

	private static final Logger log = Logger.getLogger(AbstractFeedProxy.class);

	private final MessageHub<RawMessageClass, MessageClass, KeyClass> hub;
	private final Queue<MessageClass> realtimeWaitQueue = new ConcurrentLinkedQueue<>();
	private final Queue<Long> realtimeWaitQueueCounter = new ConcurrentLinkedQueue<>();

	private CatchupState catchupState = CatchupState.CATCHUP; // start in catchup state
	private Catchup catchup = null;
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
	 * It processes the message and adds it to the event queue - or if a catchup is in
	 * progress, it adds them to the wait queue.
	 */
	@Override
	public void receive(Message parsedMsg) {
		MessageClass msg = (MessageClass) parsedMsg.message;
		// If still waiting for catchup to end, produce to the wait queue
		if (catchupState == CatchupState.CATCHUP) {
			realtimeWaitQueue.add(msg);
			realtimeWaitQueueCounter.add(parsedMsg.counter);
		} else {
			// ProcessAndQueue a message with the correct counter
			if (parsedMsg.counter == expected || !parsedMsg.checkCounter) {
				processAndQueue(parsedMsg.checkCounter ? parsedMsg.counter : expected, msg, true);
			} else if (parsedMsg.counter > expected) { // If there is a gap, try to handle it
				// If the gap was handled successfully, processAndQueue the current message
				if (handleGap(parsedMsg.counter)) {
					processAndQueue(parsedMsg.counter, msg, true);
				} else { // Else place the message into the wait queue for future processing
					log.warn("receive: Failed to handle the gap, placing msg into wait queue: " + parsedMsg.counter + ", expected: " + expected);
					realtimeWaitQueue.add(msg);
					realtimeWaitQueueCounter.add(parsedMsg.counter);
				}
			} else if (parsedMsg.counter < expected) { // Discard old messages
				log.warn("Discarding duplicate message: " + parsedMsg.counter + ", expected: " + expected);
			}
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

	/**
	 * This method checks the wait queue and/or catchup stream for missing
	 * messages. When this message returns, expected==counter must be true
	 * or else an Exception must be thrown.
	 * @param counterTarget
	 * @return true if the gap was fixed, false if not
	 */
	private boolean handleGap(long counterTarget) {
		log.info("handleGap: Trying to process events from the wait queue. Counter: " + counterTarget + ", expected: " + expected + ".");
		log.info("Wait queue contains " + realtimeWaitQueue.size() + " messages, first one has counter " + realtimeWaitQueueCounter.peek());
		
		while (expected < counterTarget) {
			
			// Purge any already-processed messages from the wait queue
			while (!realtimeWaitQueue.isEmpty() && realtimeWaitQueueCounter.peek() < expected) {
				Long waitCounter = realtimeWaitQueueCounter.poll();
				realtimeWaitQueue.poll();
				log.warn("handleGap: old message purged from wait queue: " + waitCounter + ", expected: " + expected);
			}
			
			// Is the correct counter in the wait queue?
			if (!realtimeWaitQueue.isEmpty() && realtimeWaitQueueCounter.peek()==expected) {
				Long waitCounter = realtimeWaitQueueCounter.poll();
				MessageClass waitMsg = realtimeWaitQueue.poll();
				processAndQueue(waitCounter, waitMsg, false);
				log.info("handleGap: Message processed from wait queue: "+waitCounter);
			} else if (catchup != null) { // If not, try to find the expected message in catchup
				// If the catchup lags behind, fast-forward it
				while (catchup.getNextCounter() < expected) {
					Object next = catchup.getNext();
					if (next == null) {
						log.warn("Catchup stops early at " + catchup.getNextCounter() + ", expected: " + expected + ", wait queue head: " + realtimeWaitQueueCounter.peek());
						return false;
					}
				}

				// Is the expected message found in catchup?
				int counter = catchup.getNextCounter();
				Object next = catchup.getNext();
				if (next == null) {
					log.warn("Catchup does not contain expected message: " + expected + ", wait queue head: " + realtimeWaitQueueCounter.peek());
					return false;
				} else {
					log.info("handleGap: Message processed from catchup: " + counter);
					MessageClass msg = hub.getParser().parse((RawMessageClass) next);
					processAndQueue(counter, msg, false);
				}
			} else {
				throw new RuntimeException("Message checkCounter was true, there was a sequence gap and no catchup method is set!");
			}
		}

		return true;
	}
	
	private void processAndQueue(long counter, MessageClass msg, boolean checkAge) {
		if (counter != expected) {
			throw new IllegalArgumentException("Tried to process messages in invalid order! Counter: " + counter + ", expected: " + expected);
		} else {
			expected++;
			FeedEvent<MessageClass, EventRecipientClass>[] events = process(msg);

			// TODO: remove debug
			if (firstRealQueue == null && checkAge) {
				log.info("First real time message: " + counter + ". Events: " + events);
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
	public FeedEvent[] getNextEvents() {
		if (catchup == null) {
			throw new IllegalStateException("Catchup is not started!");
		}

		FeedEvent[] result = null;
		
		while (result == null) {
			int catchupCounter = catchup.getNextCounter();
			RawMessageClass line = (RawMessageClass) catchup.getNext();

			if (line == null) {
				return null;
			}

			MessageClass msg = hub.getParser().parse(line);

			if (catchupCounter != expected) {
				throw new IllegalStateException("Gap in catchup! Counter: " + catchupCounter + ", expected: " + expected);
			} else {
				expected++;
				result = process(msg);
			}

			// Remove overlap in the wait queue
			Long cc = realtimeWaitQueueCounter.peek();
			if (cc != null && cc == catchupCounter) {
				realtimeWaitQueueCounter.poll();
				realtimeWaitQueue.poll();
			}
		}
		
		return result;
	}
	
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
	
	@Override
	public boolean startCatchup() {
		log.info("Starting catchup");
		catchup = hub.startCatchup(this);
		return catchup != null;
	}

	@Override
	public void endCatchup() {
		if (catchupState != CatchupState.CATCHUP) {
			throw new IllegalStateException("Catchup is not started!");
		}

		log.info("endCatchup called");
		catchupState = CatchupState.CATCHUP_UNSYNC_READY;
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
