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

		System.out.println("DEBUG RealtimeMessageSource.1");
		for (StreamPartition sp : streamPartitions) {
			log.info("Subscribing to stream " + sp.getStreamId() + " partition " + sp.getPartition());
			System.out.println("DEBUG RealtimeMessageSource.2");
			subscriptions.add(streamrClient.subscribe(
				streamsByStreamId.get(sp.getStreamId()),
				sp.getPartition(),
				(subscription, streamMessage) -> consumer.accept(streamMessage),
				null // no resend
			));
			System.out.println("DEBUG RealtimeMessageSource.3");
		}
	}

	@Override
	public void close() {
		System.out.println("DEBUG RealtimeMessageSource close.1");
		for (Subscription sub : subscriptions) {
			System.out.println("DEBUG RealtimeMessageSource close.2");
			streamrClient.unsubscribe(sub);
		}
	}
}
