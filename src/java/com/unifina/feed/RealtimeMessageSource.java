package com.unifina.feed;

import com.streamr.client.subs.Subscription;
import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RealtimeMessageSource extends StreamMessageSource {

	private static final Logger log = Logger.getLogger(RealtimeMessageSource.class);

	private final List<Subscription> subscriptions = new ArrayList<>();

	public RealtimeMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		for (StreamPartition sp : streamPartitions) {
			log.info("Subscribing to stream " + sp.getStreamId() + " partition " + sp.getPartition());

			subscriptions.add(streamrClient.subscribe(
				streamsByStreamId.get(sp.getStreamId()),
				sp.getPartition(),
				(subscription, streamMessage) -> {
					// TODO: remove logging
					log.info("Message received: " + streamMessage.getSerializedContent());
					consumer.accept(streamMessage);
				},
				null // no resend
			));
		}
	}

	@Override
	public void close() {
		for (Subscription sub : subscriptions) {
			streamrClient.unsubscribe(sub);
		}
	}
}
