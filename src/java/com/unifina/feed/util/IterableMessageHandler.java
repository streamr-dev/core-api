package com.unifina.feed.util;

import com.streamr.client.MessageHandler;
import com.streamr.client.Subscription;
import com.streamr.client.protocol.message_layer.StreamMessage;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IterableMessageHandler implements MessageHandler, Iterator<StreamMessage> {

	private final ArrayBlockingQueue<Object> queue; // holds StreamMessages or DoneMarker
	private final long queueOfferTimeoutSeconds;
	private final long queuePollTimeoutSeconds;

	private Object next = null; // StreamMessage or DoneMarker

	private final static Logger log = Logger.getLogger(IterableMessageHandler.class);

	public IterableMessageHandler() {
		 this(1000, 10, 20);
	}

	public IterableMessageHandler(int queueSize, long queueOfferTimeoutSeconds, long queuePollTimeoutSeconds) {
		queue = new ArrayBlockingQueue<>(queueSize, false);
		this.queueOfferTimeoutSeconds = queueOfferTimeoutSeconds;
		this.queuePollTimeoutSeconds = queuePollTimeoutSeconds;
	}

	@Override
	public void onMessage(Subscription subscription, StreamMessage streamMessage) {
		try {
			boolean success = queue.offer(streamMessage, queueOfferTimeoutSeconds, TimeUnit.SECONDS);
			if (!success) {
				log.error(String.format("onMessage: the queue is full and no space was made within %d sec. The message will be dropped.", queueOfferTimeoutSeconds));
			}
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	@Override
	public void done(Subscription sub) {
		log.info("done() called on sub: "+sub.getId());
		queue.add(new DoneMarker());
	}

	/**
	 * Blocks if there are unreceived messages
	 */
	@Override
	public boolean hasNext() {
		if (next != null && next instanceof StreamMessage) {
			return true;
		} else {
			// Queue is empty but we're not done. Let's block and see if there are more messages
			blockUntilNextIsSet();
			return next instanceof StreamMessage;
		}
	}

	@Override
	public StreamMessage next() {
		blockUntilNextIsSet();
		if (next instanceof DoneMarker) {
			throw new NoSuchElementException("No more messages!");
		} else {
			return (StreamMessage) next;
		}
	}

	private void blockUntilNextIsSet() {
		if (next != null) {
			return;
		}

		try {
			next = queue.poll(queuePollTimeoutSeconds, TimeUnit.SECONDS);
			if (next == null) {
				// Timed out
				throw new RuntimeException("Timed out while waiting for messages");
			}
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	// "Poison pill" to add to the queue to signal the end
	private static class DoneMarker {}
}
