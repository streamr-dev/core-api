package com.unifina.feed.cassandra;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.map.MapMessage;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.feed.util.MergingIterator;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

import java.util.*;

public class CassandraHistoricalFeed extends AbstractHistoricalFeed<ConfigurableStreamModule, MapMessage, String, MapMessageEventRecipient> {

	public CassandraHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception {
		return null;
	}

	@Override
	protected Iterator<FeedEvent<MapMessage, MapMessageEventRecipient>> iterator(MapMessageEventRecipient recipient) {
		// Create an iterator that merges iterators for multiple partitions by natural message order (timestamp)
		List<FeedEventIterator<MapMessage, MapMessageEventRecipient>> iterators = new ArrayList<>(recipient.getPartitions().size());
		for (Integer partition : recipient.getPartitions()) {
			iterators.add(new FeedEventIterator<>(new CassandraHistoricalIterator(recipient.getStream(), partition, globals.getStartDate(), globals.getEndDate()), this, recipient));
		}

		return new MergingIterator<>(iterators, new Comparator<FeedEvent<MapMessage, MapMessageEventRecipient>>() {
			@Override
			public int compare(FeedEvent<MapMessage, MapMessageEventRecipient> o1, FeedEvent<MapMessage, MapMessageEventRecipient> o2) {
				return o1.compareTo(o2);
			}
		});
	}
}
