package com.unifina.feed.twitter;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

import java.util.ArrayList;
import java.util.List;

public class TwitterFeed extends AbstractFeedProxy<TwitterModule, twitter4j.Status, TwitterMessage, String, TwitterEventRecipient> {

	public TwitterFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent<TwitterMessage, TwitterEventRecipient>[] process(TwitterMessage msg) {
		List<FeedEvent> events = new ArrayList<>();

		// find streams whose keywords are found within tweet, forward to each of them
		//   ("demux", see "mux" in TwitterMessageSource.updateTwitterStreamFor)
		String tweet = TwitterStreamConfig.getSearchStringFromTwitterStatus(msg.status);
		for (TwitterEventRecipient er : eventRecipients) {
			TwitterStreamConfig conf = TwitterStreamConfig.forStream(er.getStream());

			for (String kw : conf.getKeywords()) {
				if (tweet.contains(kw)) {
					FeedEvent e = new FeedEvent(msg, msg.timestamp, er);
					events.add(e);
					break;
				}
			}
		}

		return events.toArray(new FeedEvent[events.size()]);
	}
}
