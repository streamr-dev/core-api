package com.unifina.feed.mongodb;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.feed.kafka.KafkaMessage;
import com.unifina.feed.map.MapMessage;
import com.unifina.utils.Globals;
import grails.converters.JSON;

import java.util.Map;

public class MongoKeyProvider extends AbstractKeyProvider<IStreamRequirement, MapMessage, Stream> {

	public MongoKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public Stream getSubscriberKey(IStreamRequirement subscriber) {
		return subscriber.getStream();
	}

	@Override
	public Stream getMessageKey(MapMessage message) {
		return ((DocumentFromStream)message.payload).getStream();
	}

}
