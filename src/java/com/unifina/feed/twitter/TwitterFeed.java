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
		if (eventRecipients.size() == 0) {
			events = new FeedEvent[] {};
		} else if (eventRecipients.size() == 1) {
			FeedEvent e = new FeedEvent(msg, msg.getTimestamp(), eventRecipients.get(0));
			events = new FeedEvent[] {e};
		} else {
			List<FeedEvent> eventList = new ArrayList<>();
			for (TwitterEventRecipient er : eventRecipients) {
				if (er.getStream().getId().equals(msg.streamConfig.getStreamId())) {
					FeedEvent e = new FeedEvent(msg, msg.getTimestamp(), er);
					eventList.add(e);
				}
			}
			events = eventList.toArray(new FeedEvent[eventList.size()]);
		}
		return events;
	}
}
