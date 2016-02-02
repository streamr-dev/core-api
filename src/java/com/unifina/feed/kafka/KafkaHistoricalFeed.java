package com.unifina.feed.kafka;

import com.unifina.utils.TimeOfDayUtil;
import grails.converters.JSON;

import java.io.Closeable;
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
import com.unifina.feed.FeedEventIterator;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.kafkaclient.UnifinaKafkaIterator;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;
import org.apache.commons.lang.time.DateUtils;

public class KafkaHistoricalFeed extends AbstractHistoricalFileFeed {

	Map<Stream, Boolean> kafkaIteratorReturnedForStream = new HashMap<>();
	Properties kafkaProperties;
	
	public KafkaHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);

		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(globals.getGrailsApplication().getConfig(), "unifina.kafka"));
		kafkaProperties = new Properties();
		for (String s : kafkaConfig.keySet())
			kafkaProperties.setProperty(s, kafkaConfig.get(s).toString());
	}

	@Override
	protected Date getTimestamp(Object eventContent,
			Iterator<? extends Object> contentIterator) {
		return ((KafkaMessage)eventContent).timestamp;
	}

	@Override
	protected Stream getStream(IEventRecipient recipient) {
		return ((StreamEventRecipient)recipient).getStream();
	}

	@Override
	protected Iterator<KafkaMessage> createContentIterator(FeedFile feedFile, Date day,
			InputStream inputStream, IEventRecipient recipient) {
		try {
			Map streamConfig = getStream(recipient).getStreamConfigAsMap();
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

		// Only check Kafka for further messages if the latest message was not on the end date
		if (iterator==null && !kafkaIteratorReturnedForStream.containsKey(stream) && !TimeOfDayUtil.getMidnight(globals.time).equals(TimeOfDayUtil.getMidnight(globals.getEndDate()))) {
			kafkaIteratorReturnedForStream.put(stream, true);

			Map streamConfig = ((Map)JSON.parse(getStream(recipient).getStreamConfig()));
			UnifinaKafkaIterator kafkaIterator = new UnifinaKafkaIterator(streamConfig.get("topic").toString(), globals.time, globals.getEndDate(), 10*1000, kafkaProperties);
			
			// UnifinaKafkaIterator iterates over raw UnifinaKafkaMessages, 
			// so need to wrap it with a parsing iterator
			iterator = new FeedEventIterator(new ParsingKafkaIterator(kafkaIterator), this, recipient);
		}
		
		return iterator;
	}

	class ParsingKafkaIterator implements Iterator<KafkaMessage>, Closeable {

		KafkaMessageParser parser = new KafkaMessageParser();
		UnifinaKafkaIterator kafkaIterator;

		public ParsingKafkaIterator(UnifinaKafkaIterator kafkaIterator) {
			this.kafkaIterator = kafkaIterator;
		}

		@Override
		public boolean hasNext() {
			return kafkaIterator.hasNext();
		}

		@Override
		public KafkaMessage next() {
			return parser.parse(kafkaIterator.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() throws IOException {
			kafkaIterator.close();
		}
	}

}
