package com.unifina.feed.twitter;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

import java.util.Collections;
import java.util.List;

public class TwitterKeyProvider extends AbstractKeyProvider<TwitterModule, TwitterMessage, String> {

	public TwitterKeyProvider(Globals globals, Feed feed) {
			super(globals, feed);
	}

	@Override
	public List<String> getSubscriberKeys(TwitterModule subscriber) {
		return Collections.singletonList(subscriber.getStream().getId());
	}

	@Override
	public String getMessageKey(TwitterMessage message) {
		return "twitter";
	}
}
