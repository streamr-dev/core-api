package com.unifina.feed.mongodb;

import com.unifina.data.IEventRecipient;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.ITimestamped;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.feed.map.MapMessage;
import com.unifina.feed.map.MapMessageEventRecipient;
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
 * 			"pollIntervalMillis": 100,
 *	 		"query": "{ \"ue_id\": ObjectId(\"5649955225b568505b60bc31\") }" // optional - must be a string, as this is not valid JSON!
 * 		}
 * 	}
 */
public class MongoHistoricalFeed extends AbstractHistoricalFeed<IStreamRequirement, MapMessage, Stream, MapMessageEventRecipient> {

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
	protected FeedEventIterator<MapMessage, MapMessageEventRecipient> getNextIterator(MapMessageEventRecipient recipient) throws IOException {
		if (iteratorReturned.contains(recipient)) {
			return null;
		} else {
			iteratorReturned.add(recipient);
			Stream stream = recipient.getStream();
			return new FeedEventIterator<>(new MongoHistoricalIterator(stream, globals.getStartDate(), globals.getEndDate()), this, recipient);
		}
	}

}
