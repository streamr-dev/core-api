package com.unifina.feed.mongodb;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

/**
 * Example streamConfig:
 *
 * 	{
 * 		"mongodb": {
 * 			"host": "dev.streamr",
 * 			"database": "test",
 * 			"collection": "MongoHistoricalIteratorSpec",
 * 			"timestampKey": "time"
 * 		}
 * 	}
 */
public class MongoHistoricalFeed extends AbstractHistoricalFeed {

	public MongoHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	public List<Date[]> getUnitsBetween(Date beginDate, Date endDate) throws Exception {
		List<Date[]> result = new ArrayList(1);
		result.add(new Date[] {beginDate, endDate});
		return result;
	}

	@Override
	protected FeedEventIterator getNextIterator(IEventRecipient recipient) throws IOException {
		Stream stream = ((StreamEventRecipient)recipient).getStream();
		return new FeedEventIterator(new MongoHistoricalIterator(stream, globals.getStartDate(), globals.getEndDate()), this, recipient);
	}

	@Override
	protected Date getTimestamp(Object eventContent, Iterator<? extends Object> contentIterator) {
		return ((Document) eventContent).getDate(((MongoHistoricalIterator) contentIterator).getTimestampKey());
	}
}
