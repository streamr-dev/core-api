package com.unifina.feed.kafka;

import grails.converters.JSON;

import java.util.Map;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractKeyProvider;
import com.unifina.utils.Globals;

public class KafkaKeyProvider extends AbstractKeyProvider {

	public KafkaKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public Object getSubscriberKey(Object subscriber) {
		return ((Map)JSON.parse(((IStreamRequirement)subscriber).getStream().getStreamConfig())).get("topic").toString();
	}

	@Override
	public Object getMessageKey(Object message) {
		return ((KafkaMessage)message).topic;
	}

}
