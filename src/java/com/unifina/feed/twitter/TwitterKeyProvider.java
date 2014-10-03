package com.unifina.feed.twitter;

import java.util.HashSet;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

public class TwitterKeyProvider extends AbstractKeyProvider {

	HashSet<String> keywords = new HashSet<>();
	
	public TwitterKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public Object getEventRecipientKey(Object subscriber) {
		return ((TwitterModule)subscriber).keyword.getValue().toLowerCase();
	}

	@Override
	public Object getMessageKey(Object message) {
		throw new RuntimeException("Cannot get subscription key from message, must lookup recipient within feed impl!");
	}

}
