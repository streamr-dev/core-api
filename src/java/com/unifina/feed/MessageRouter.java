package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

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
	public void subscribe(Consumer<StreamMessage> consumer, StreamPartition streamPartition) {
		synchronized (consumersByStreamPartition) {
			if (!consumersByStreamPartition.containsKey(streamPartition)) {
				consumersByStreamPartition.put(streamPartition, new ArrayList<>());
			}
		}

		List<Consumer<StreamMessage>> list = consumersByStreamPartition.get(streamPartition);

		synchronized (list) {
			if (!list.contains(consumer)) {
				list.add(consumer);
			}
		}
	}

	public void unsubscribe(Consumer<StreamMessage> consumer, StreamPartition streamPartition) {
		List<Consumer<StreamMessage>> list = consumersByStreamPartition.get(streamPartition);

		if (list != null) {
			synchronized (list) {
				list.remove(consumer);
				if (list.isEmpty()) {
					log.info("unsubscribe: No more consumers for" + streamPartition);
					consumersByStreamPartition.remove(streamPartition);
				}
			}
		}
	}

}
