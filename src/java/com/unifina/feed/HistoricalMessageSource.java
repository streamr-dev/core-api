package com.unifina.feed;

import com.streamr.client.options.ResendRangeOption;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.feed.util.IterableMessageHandler;
import com.unifina.feed.util.MergingIterator;
import com.unifina.feed.util.StreamMessageComparator;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Interleaves historical messages from the given StreamPartitions into one timeline using a
 * MergingIterator, and starts a Thread that passes messages from that MergingIterator to the
 * given Consumer (i.e. bridges from "pull" to "push").
 */
public class HistoricalMessageSource extends StreamMessageSource implements Iterable<StreamMessage> {

	private static final Logger log = Logger.getLogger(HistoricalMessageSource.class);

	private final MergingIterator<StreamMessage> mergingIterator;
	private boolean quit = false;

	public HistoricalMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		List<Iterator<StreamMessage>> resendIterators = new ArrayList<>(streamPartitions.size());

		for (StreamPartition sp : streamPartitions) {
			IterableMessageHandler iterator = new IterableMessageHandler();
			resendIterators.add(iterator);

			streamrClient.resend(
				streamsByStreamId.get(sp.getStreamId()),
				sp.getPartition(),
				iterator,
				new ResendRangeOption(globals.getStartDate(), globals.getEndDate())
			);
		}

		mergingIterator = new MergingIterator<>(resendIterators, new StreamMessageComparator());
		Thread reporterThread = new Thread(() -> {
			while (mergingIterator.hasNext() && !quit) {
				consumer.accept(mergingIterator.next());
			}
			consumer.done();
		});
		reporterThread.setName("HistoricalMessageSource-" + System.currentTimeMillis());
		reporterThread.start();
	}

	@Override
	public void close() {
		quit = true; // signals the reporterThread to stop
		mergingIterator.close();
	}

	@NotNull
	@Override
	public Iterator<StreamMessage> iterator() {
		return mergingIterator;
	}
}
