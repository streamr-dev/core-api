package com.unifina.feed.kafka;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.feed.StreamrMessage;
import com.unifina.utils.Globals;

public class KafkaKeyProvider extends AbstractKeyProvider<IStreamRequirement, StreamrMessage, String> {

	public KafkaKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public String getSubscriberKey(IStreamRequirement subscriber) {
		return subscriber.getStream().getId();
	}

	@Override
	public String getMessageKey(StreamrMessage message) {
		return message.streamId;
	}

}
