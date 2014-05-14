package com.unifina.feed.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import scala.actors.threadpool.Arrays;

import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractHistoricalFileFeed;
import com.unifina.signalpath.kafka.KafkaModule;
import com.unifina.utils.Globals;

public class KafkaHistoricalFeed extends AbstractHistoricalFileFeed {

	public KafkaHistoricalFeed(Globals globals) {
		super(globals);
	}

	@Override
	protected Date getTimestamp(Object eventContent,
			Iterator<Object> contentIterator) {
		return ((KafkaMessage)eventContent).timestamp;
	}

	@Override
	protected Stream getStream(IEventRecipient recipient) {
		return ((KafkaMessageRecipient)recipient).getStream();
	}

	@Override
	protected Iterator<Object> createContentIterator(Date day,
			InputStream inputStream, IEventRecipient recipient) {
		try {
			return new KafkaHistoricalIterator(inputStream,((KafkaMessageRecipient)recipient).getTopic());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected List<Class> getValidSubscriberClasses() {
		return Arrays.asList(new Class[] {KafkaModule.class});
	}
	
	@Override
	protected Object getEventRecipientKey(Object subscriber) {
		return ((KafkaModule)subscriber).getTopic();
	}

	@Override
	protected IEventRecipient doCreateEventRecipient(Object subscriber) {
		KafkaModule m = (KafkaModule)subscriber;
		return new KafkaMessageRecipient(globals,m.getStream(),m.getTopic());
	}

}
