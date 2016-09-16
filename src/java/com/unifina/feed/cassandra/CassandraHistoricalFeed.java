package com.unifina.feed.cassandra;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.map.MapMessage;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CassandraHistoricalFeed extends AbstractHistoricalFeed<ConfigurableStreamModule, MapMessage, String, MapMessageEventRecipient> {

	private boolean singletonIteratorReturned = false;

	public CassandraHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception {
		return null;
	}

	@Override
	protected FeedEventIterator<MapMessage, MapMessageEventRecipient> getNextIterator(MapMessageEventRecipient recipient) throws IOException {
		if (!singletonIteratorReturned) {
			singletonIteratorReturned = true;
			return new FeedEventIterator<>(new CassandraHistoricalIterator(recipient.getStream(), globals.getStartDate(), globals.getEndDate()), this, recipient);
		} else {
			return null;
		}
	}

}
