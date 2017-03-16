package com.unifina.feed.twitter;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.feed.StreamrMessageEventRecipient;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

public class TwitterFeed extends AbstractFeedProxy<TwitterModule, TwitterMessage, TwitterMessage, String, StreamrMessageEventRecipient> {

	public TwitterFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

}
