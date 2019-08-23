package com.unifina.feed.cassandra;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.feed.StreamMessageSource;
import com.unifina.feed.util.MergingIterator;
import com.unifina.feed.util.StreamMessageComparator;
import com.unifina.utils.Globals;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interleaves historical messages from the given StreamPartitions into one timeline using a
 * MergingIterator, and starts a Thread that passes messages from that MergingIterator to the
 * given Consumer (i.e. bridges from "pull" to "push").
 */
public class CassandraMessageSource extends StreamMessageSource implements Iterable<StreamMessage> {

	private final MergingIterator<StreamMessage> mergingIterator;
	private boolean quit = false;

	public CassandraMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		super(consumer, streamPartitions);

		List<CassandraHistoricalIterator> iterators = streamPartitions.stream()
			.map(streamPartition -> new CassandraHistoricalIterator(streamPartition, globals.getStartDate(), globals.getEndDate()))
			.collect(Collectors.toList());

		mergingIterator = new MergingIterator<>(iterators, new StreamMessageComparator());
		Thread reporterThread = new Thread(() -> {
			while (mergingIterator.hasNext() && !quit) {
				consumer.accept(mergingIterator.next());
			}
			consumer.done();
		});
		reporterThread.setName("CassandraMessageSource-" + System.currentTimeMillis());
		reporterThread.start();
	}

	@Override
	public void close() throws IOException {
		quit = true; // signals the reporterThread to stop
		mergingIterator.close();
	}

	@NotNull
	@Override
	public Iterator<StreamMessage> iterator() {
		return mergingIterator;
	}
}
