package com.unifina.feed.mongodb;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.feed.StreamrMessageEventRecipient;
import com.unifina.utils.Globals;

import java.util.Iterator;

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
public class MongoHistoricalFeed extends AbstractHistoricalFeed<IStreamRequirement, MongoMessage, Stream, StreamrMessageEventRecipient> {

	public MongoHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected Iterator<MongoMessage> createContentIterator(StreamrMessageEventRecipient recipient) {
		return new MongoHistoricalIterator(recipient.getStream(), globals.getStartDate(), globals.getEndDate());
	}

}
