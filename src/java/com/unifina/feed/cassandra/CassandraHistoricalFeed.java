package com.unifina.feed.cassandra;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.feed.util.MergingIterator;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

import java.util.*;

public class CassandraHistoricalFeed extends AbstractHistoricalFeed<ConfigurableStreamModule, StreamMessage, String, MapMessageEventRecipient> {

	public CassandraHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception {
		return null;
	}

	@Override
	protected Iterator<FeedEvent<StreamMessage, MapMessageEventRecipient>> iterator(MapMessageEventRecipient recipient) {
		// Create an iterator that merges iterators for multiple partitions by natural message order (timestamp)
		List<FeedEventIterator<StreamMessage, MapMessageEventRecipient>> iterators = new ArrayList<>(recipient.getPartitions().size());
		for (Integer partition : recipient.getPartitions()) {
			iterators.add(new FeedEventIterator<>(new CassandraHistoricalIterator(recipient.getStream(), partition, globals.getStartDate(), globals.getEndDate()), this, recipient));
		}

		return new MergingIterator<>(iterators, new Comparator<FeedEvent<StreamMessage, MapMessageEventRecipient>>() {
			@Override
			public int compare(FeedEvent<StreamMessage, MapMessageEventRecipient> o1, FeedEvent<StreamMessage, MapMessageEventRecipient> o2) {
				return o1.compareTo(o2);
			}
		});
	}
}
