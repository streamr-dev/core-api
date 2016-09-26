package com.unifina.feed.kafka;

import com.google.common.collect.Iterators;
import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.feed.*;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.kafkaclient.UnifinaKafkaIterator;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;
import com.unifina.utils.TimeOfDayUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class KafkaHistoricalFeed extends AbstractHistoricalFileFeed<IStreamRequirement, StreamrMessage, String, MapMessageEventRecipient> {

	Properties kafkaProperties;

	public KafkaHistoricalFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);

		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(globals.getGrailsApplication().getConfig(), "unifina.kafka"));
		kafkaProperties = new Properties();
		for (String s : kafkaConfig.keySet())
			kafkaProperties.setProperty(s, kafkaConfig.get(s).toString());
	}

	@Override
	protected Stream getStream(IEventRecipient recipient) {
		return ((StreamEventRecipient)recipient).getStream();
	}

	@Override
	protected Iterator<StreamrMessage> createContentIterator(FeedFile feedFile, Date day,
															 InputStream inputStream, MapMessageEventRecipient recipient) {
		try {
			return new KafkaHistoricalIterator(inputStream,feedFile.getStream().getId());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Iterator<FeedEvent<StreamrMessage, MapMessageEventRecipient>> iterator(final MapMessageEventRecipient recipient) {
		Iterator<FeedEvent<StreamrMessage, MapMessageEventRecipient>> fileFeedIterator = super.iterator(recipient);
		return Iterators.concat(fileFeedIterator, new TimeCheckingKafkaContinuationIterator(fileFeedIterator, recipient, this));
	}

	class TimeCheckingKafkaContinuationIterator implements Iterator<FeedEvent<StreamrMessage, MapMessageEventRecipient>> {
		private final boolean feedFileIteratorHadMessages;
		private final MapMessageEventRecipient recipient;
		private final AbstractHistoricalFeed feed;
		private Boolean cachedContinuationCondition = null;
		private Iterator<FeedEvent<StreamrMessage, MapMessageEventRecipient>> iterator;

		public TimeCheckingKafkaContinuationIterator(Iterator<FeedEvent<StreamrMessage, MapMessageEventRecipient>> feedFileIterator, MapMessageEventRecipient recipient, AbstractHistoricalFeed feed) {
			this.recipient = recipient;
			this.feedFileIteratorHadMessages = (feedFileIterator != null && feedFileIterator.hasNext());
			this.feed = feed;
		}

		private void ensureIterator() {
			if (iterator==null) {
				UnifinaKafkaIterator kafkaIterator = new UnifinaKafkaIterator(recipient.getStream().getId(), globals.time, globals.getEndDate(), 10*1000, kafkaProperties);

				// UnifinaKafkaIterator iterates over raw UnifinaKafkaMessages,
				// so need to wrap it with a parsing iterator
				iterator = new FeedEventIterator<>(new ParsingKafkaIterator(kafkaIterator), feed, recipient);
			}
		}

		// Optimisation: don't look into Kafka if the feedFileIterator had messages reaching until the end date
		private boolean checkContinuationCondition() {
			if (cachedContinuationCondition == null) {
				cachedContinuationCondition = !(feedFileIteratorHadMessages && TimeOfDayUtil.getMidnight(globals.time).equals(TimeOfDayUtil.getMidnight(globals.getEndDate())));
			}
			return cachedContinuationCondition;
		}

		@Override
		public boolean hasNext() {
			if (checkContinuationCondition()) {
				return false;
			}

			ensureIterator();
			return iterator.hasNext();
		}

		@Override
		public FeedEvent<StreamrMessage, MapMessageEventRecipient> next() {
			if (checkContinuationCondition()) {
				return null;
			}

			ensureIterator();
			return iterator.next();
		}

		@Override
		public void remove() {

		}
	}

	class ParsingKafkaIterator implements Iterator<StreamrMessage>, Closeable {

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
		public StreamrMessage next() {
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
