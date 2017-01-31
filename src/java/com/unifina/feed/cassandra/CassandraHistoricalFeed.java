package com.unifina.feed.cassandra;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.AbstractStreamrMessage;
import com.unifina.feed.StreamrMessageEventRecipient;
import com.unifina.feed.util.MergingIterator;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CassandraHistoricalFeed extends AbstractHistoricalFeed<ConfigurableStreamModule, AbstractStreamrMessage, String, StreamrMessageEventRecipient> {

	public CassandraHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected Iterator<AbstractStreamrMessage> createContentIterator(StreamrMessageEventRecipient recipient) {
		// Create an iterator that merges iterators for multiple partitions by natural message order (timestamp)
		List<CassandraHistoricalIterator> iterators = new ArrayList<>(recipient.getPartitions().size());
		for (Integer partition : recipient.getPartitions()) {
			iterators.add(new CassandraHistoricalIterator(recipient.getStream(), partition, globals.getStartDate(), globals.getEndDate()));
		}

		return new MergingIterator<>(iterators, new Comparator<AbstractStreamrMessage>() {
			@Override
			public int compare(AbstractStreamrMessage o1, AbstractStreamrMessage o2) {
				return o1.compareTo(o2);
			}
		});
	}
}
