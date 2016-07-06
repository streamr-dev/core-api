package com.unifina.feed.twitter;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

public class TwitterFeed extends AbstractFeedProxy<TwitterModule, twitter4j.Status, TwitterMessage, String, TwitterEventRecipient> {

	public TwitterFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent<TwitterMessage, TwitterEventRecipient>[] process(TwitterMessage msg) {
		if (eventRecipients.size() < 1) { return new FeedEvent[] {}; }

		FeedEvent e = new FeedEvent(msg, msg.timestamp, eventRecipients.get(0));

		return new FeedEvent[] {e};
	}
}
