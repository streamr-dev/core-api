package com.unifina.feed.kafka;

import grails.converters.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFileFeed;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.kafkaclient.UnifinaKafkaConsumer;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;

public class KafkaHistoricalFeed extends AbstractHistoricalFileFeed {

	Map<Stream, Boolean> kafkaIteratorReturnedForStream = new HashMap<>();
	
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

	@Override
	protected FeedEventIterator getNextIterator(IEventRecipient recipient)
			throws IOException {
		FeedEventIterator iterator = super.getNextIterator(recipient);
		
		Stream stream = getStream(recipient);
		if (iterator==null && !kafkaIteratorReturnedForStream.containsKey(stream)) {
			kafkaIteratorReturnedForStream.put(stream, true);
			
			Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(globals.getGrailsApplication().getConfig(), "unifina.kafka"));
			Properties properties = new Properties();
			for (String s : kafkaConfig.keySet())
				properties.setProperty(s, kafkaConfig.get(s).toString());
			
			UnifinaKafkaConsumer consumer = new UnifinaKafkaConsumer(properties);
		}
		
		return iterator;
	}
	
}
