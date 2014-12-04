package com.unifina.feed.kafka;

import grails.converters.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFileFeed;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.utils.Globals;

public class KafkaHistoricalFeed extends AbstractHistoricalFileFeed {

	public KafkaHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected Date getTimestamp(Object eventContent,
			Iterator<Object> contentIterator) {
		return ((KafkaMessage)eventContent).timestamp;
	}

	@Override
	protected Stream getStream(IEventRecipient recipient) {
		return ((StreamEventRecipient)recipient).getStream();
	}

	@Override
	protected Iterator<Object> createContentIterator(FeedFile feedFile, Date day,
			InputStream inputStream, IEventRecipient recipient) {
		try {
			Map streamConfig = ((Map)JSON.parse(getStream(recipient).getStreamConfig()));
			return new KafkaHistoricalIterator(inputStream,streamConfig.get("topic").toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
