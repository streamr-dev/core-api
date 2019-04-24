package com.unifina.feed.cassandra;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.feed.StreamMessageSource;
import com.unifina.feed.util.MergingIterator;
import com.unifina.feed.util.StreamMessageComparator;
import com.unifina.utils.Globals;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CassandraMessageSource extends StreamMessageSource {

	private final MergingIterator<StreamMessage> mergingIterator;
	private boolean quit = false;

	public CassandraMessageSource(Globals globals, Consumer<StreamMessage> consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		List<CassandraHistoricalIterator> iterators = streamPartitions.stream()
			.map(sp -> new CassandraHistoricalIterator(sp, globals.getStartDate(), globals.getEndDate()))
			.collect(Collectors.toList());

		mergingIterator = new MergingIterator<>(iterators, new StreamMessageComparator());
		Thread reporterThread = new Thread(() -> {
			while (mergingIterator.hasNext() && !quit) {
				consumer.accept(mergingIterator.next());
			}
		});
		reporterThread.setName("CassandraMessageSource-" + System.currentTimeMillis());
		reporterThread.start();
	}

	@Override
	public void close() throws IOException {
		quit = true; // signals the reporterThread to stop
		mergingIterator.close();
	}

}
