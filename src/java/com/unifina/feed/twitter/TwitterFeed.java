package com.unifina.feed.twitter;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

import java.util.ArrayList;
import java.util.List;

public class TwitterFeed extends AbstractFeedProxy<TwitterModule, TwitterMessage, TwitterMessage, String, TwitterEventRecipient> {

	public TwitterFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent<TwitterMessage, TwitterEventRecipient>[] process(TwitterMessage msg) {
		FeedEvent[] events;
		if (eventRecipients.size() > 0) {
			TwitterEventRecipient recipient = eventRecipients.get(0);
			if (eventRecipients.size() > 1) {
				for (int i = 1; i < eventRecipients.size(); i++) {
					TwitterEventRecipient er = eventRecipients.get(i);
					if (er.getStream().getId().equals(msg.streamConfig.getStreamId())) {
						recipient = er;
					}
				}
			}
			FeedEvent e = new FeedEvent(msg, msg.getTimestamp(), recipient);
			events = new FeedEvent[] {e};
		} else {
			events = new FeedEvent[] {};
		}
		return events;
	}
}
