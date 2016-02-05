package com.unifina.feed.kafka;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.utils.Globals;

public class KafkaKeyProvider extends AbstractKeyProvider<IStreamRequirement,KafkaMessage> {

	public KafkaKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public Object getSubscriberKey(IStreamRequirement subscriber) {
		return subscriber.getStream().getUuid();
	}

	@Override
	public Object getMessageKey(KafkaMessage message) {
		return message.topic;
	}

}
