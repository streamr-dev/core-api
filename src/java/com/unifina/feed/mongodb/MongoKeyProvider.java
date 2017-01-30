package com.unifina.feed.mongodb;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.utils.Globals;

import java.util.Arrays;
import java.util.List;

public class MongoKeyProvider extends AbstractKeyProvider<IStreamRequirement, MongoMessage, Stream> {

	public MongoKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public List<Stream> getSubscriberKeys(IStreamRequirement subscriber) {
		return Arrays.asList(subscriber.getStream());
	}

	@Override
	public Stream getMessageKey(MongoMessage message) {
		return message.getStream();
	}

}
