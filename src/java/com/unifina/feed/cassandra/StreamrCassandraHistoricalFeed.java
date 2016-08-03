package com.unifina.feed.cassandra;

import com.unifina.domain.data.Feed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.map.MapMessage;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.utils.Globals;

import java.io.IOException;

/**
 * Like CassandraHistoricalFeed, but creates a StreamrCassandraHistoricalIterator instead of a CassandraHistoricalIterator
 * to connect to the Streamr Cassandra cluster configured in Grails config (instead of a user-defined one configured in
 * Stream config).
 */
public class StreamrCassandraHistoricalFeed extends CassandraHistoricalFeed {

	public StreamrCassandraHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEventIterator<MapMessage, MapMessageEventRecipient> getNextIterator(MapMessageEventRecipient recipient) throws IOException {
		return new FeedEventIterator<>(new StreamrCassandraHistoricalIterator(recipient.getStream(), globals.getStartDate(), globals.getEndDate()), this, recipient);
	}

}
