package com.unifina.feed.mongodb;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.feed.map.StreamrMessageEventRecipient;
import com.unifina.utils.Globals;

public class MongoFeed extends AbstractFeedProxy<IStreamRequirement, MongoMessage, MongoMessage, Stream, StreamrMessageEventRecipient> {

	public MongoFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

}
