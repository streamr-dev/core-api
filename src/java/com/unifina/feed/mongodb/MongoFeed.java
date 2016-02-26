package com.unifina.feed.mongodb;

import com.unifina.data.FeedEvent;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.feed.map.MapMessage;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.utils.Globals;

/**
 * Created by henripihkala on 09/02/16.
 */
public class MongoFeed extends AbstractFeedProxy<IStreamRequirement, MapMessage, MapMessage, Stream, MapMessageEventRecipient> {

	public MongoFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent[] process(MapMessage msg) {
		FeedEvent e = new FeedEvent(msg,
				msg.timestamp,
				getEventRecipientForMessage(msg));

		e.feed = this;

		return new FeedEvent[] {e};
	}
}
