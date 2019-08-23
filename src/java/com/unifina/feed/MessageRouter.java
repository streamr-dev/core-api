package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A helper class which maintains a List of Consumers for each StreamPartition
 * and routes StreamMessages to them.
 */
public class MessageRouter {

	private static final Logger log = Logger.getLogger(MessageRouter.class);
	private final Map<StreamPartition, List<Consumer<StreamMessage>>> consumersByStreamPartition = new HashMap<>();

	public Collection<Consumer<StreamMessage>> route(StreamMessage m) {
		StreamPartition streamPartition = new StreamPartition(m.getStreamId(), m.getStreamPartition());
		List<Consumer<StreamMessage>> consumersForMessage = consumersByStreamPartition.get(streamPartition);
		if (consumersForMessage == null) {
			return Collections.emptyList();
		} else {
			return consumersForMessage;
		}
	}

	/**
	 * Lets the router know that the given Consumer is interested in messages from the
	 * the given StreamPartition. Such messages are routed to the given Consumer when encountered.
	 */
	public synchronized void subscribe(Consumer<StreamMessage> consumer, StreamPartition streamPartition) {
		if (!consumersByStreamPartition.containsKey(streamPartition)) {
			// CopyOnWriteArray is chosen because it avoids synchronization on read/traversal (for speed).
			// This choice contains the assumption that reads far outweigh writes to this list.
			consumersByStreamPartition.put(streamPartition, new CopyOnWriteArrayList<>());
		}

		List<Consumer<StreamMessage>> list = consumersByStreamPartition.get(streamPartition);

		if (!list.contains(consumer)) {
			list.add(consumer);
		}
	}

	public synchronized void unsubscribe(Consumer<StreamMessage> consumer, StreamPartition streamPartition) {
		List<Consumer<StreamMessage>> list = consumersByStreamPartition.get(streamPartition);

		if (list != null) {
			list.remove(consumer);
			if (list.isEmpty()) {
				log.info("unsubscribe: No more consumers for" + streamPartition);
				consumersByStreamPartition.remove(streamPartition);
			}
		}
	}

}
