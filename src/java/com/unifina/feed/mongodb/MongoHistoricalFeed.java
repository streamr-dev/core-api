package com.unifina.feed.mongodb;

import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.feed.map.MapMessage;
import com.unifina.utils.Globals;
import org.bson.Document;

import java.io.IOException;
import java.util.*;

/**
 * Example streamConfig:
 *
 * 	{
 * 		"mongodb": {
 * 			"host": "dev.streamr",
 * 			"port": 27017, // optional
 * 			"username: "", // optional
 * 			"password: "", // optional
 * 			"database": "test",
 * 			"collection": "MongoHistoricalIteratorSpec",
 * 			"timestampKey": "time",
 *	 		"query": "{ \"ue_id\": ObjectId(\"5649955225b568505b60bc31\") }" // optional - must be a string, as this is not valid JSON!
 * 		}
 * 	}
 */
public class MongoHistoricalFeed extends AbstractHistoricalFeed<MapMessage> {

	private HashSet<IEventRecipient> iteratorReturned = new HashSet<>();

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
		if (iteratorReturned.contains(recipient))
			return null;

		else {
			iteratorReturned.add(recipient);
			Stream stream = ((StreamEventRecipient)recipient).getStream();
			return new FeedEventIterator(new MongoHistoricalIterator(stream, globals.getStartDate(), globals.getEndDate()), this, recipient);
		}
	}

	@Override
	protected Date getTimestamp(MapMessage eventContent, Iterator<MapMessage> contentIterator) {
		return eventContent.timestamp;
	}
}
