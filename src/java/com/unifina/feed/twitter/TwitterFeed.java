package com.unifina.feed.twitter;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

import java.util.ArrayList;
import java.util.List;

public class TwitterFeed extends AbstractFeedProxy<TwitterModule, TwitterMessage, TwitterMessage, String, TwitterEventRecipient> {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TwitterMessageSource.class);

	public TwitterFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	// TwitterFeed.process should ever receive messages that are meant for a single stream
	// "Demux" happens already in TwitterMessageSource.SubscribedUser.onStatus, and it passes
	//	 one copy of TwitterMessage for each keyword-matched Stream to process(msg)
	@Override
	protected FeedEvent<TwitterMessage, TwitterEventRecipient>[] process(TwitterMessage msg) {
		TwitterEventRecipient recipient = null;
		int erCount = eventRecipients.size();
		for (int i = 0; i < erCount; i++) {
			TwitterEventRecipient er = eventRecipients.get(i);
			if (er.getStream().getId().equals(msg.streamConfig.getStreamId())) {
				recipient = er;
				break;
			}
		}

		FeedEvent[] events;
		if (recipient != null) {
			FeedEvent e = new FeedEvent(msg, msg.getTimestamp(), recipient);
			events = new FeedEvent[] {e};
		} else {
			events = new FeedEvent[] {};
			log.error("Found no recipient (out of " + erCount + " recipients) for TwitterMessage " + msg);
		}
		return events;
	}
}
