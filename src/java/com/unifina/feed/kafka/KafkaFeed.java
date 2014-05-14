package com.unifina.feed.kafka;

import java.util.List;

import scala.actors.threadpool.Arrays;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.signalpath.kafka.KafkaModule;
import com.unifina.utils.Globals;

public class KafkaFeed extends AbstractFeedProxy<UnifinaKafkaMessage, KafkaMessage> {
	
	public KafkaFeed(Globals globals) {
		super(globals);
	}

	@Override
	protected FeedEvent[] process(KafkaMessage msg) {
		FeedEvent e = new FeedEvent(msg, 
				msg.timestamp,
				eventRecipientsByKey.get(msg.topic));
		
		e.feed = this; 
				
		return new FeedEvent[] {e};
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
	protected IEventRecipient createEventRecipient(Object subscriber) {
		KafkaModule m = (KafkaModule)subscriber;
		return new KafkaMessageRecipient(globals,m.getStream(),m.getTopic());
	}

}
