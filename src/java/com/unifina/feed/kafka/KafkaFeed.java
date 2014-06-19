package com.unifina.feed.kafka;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.utils.Globals;

public class KafkaFeed extends AbstractFeedProxy<UnifinaKafkaMessage, KafkaMessage> {
	
	public KafkaFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent[] process(KafkaMessage msg) {
		FeedEvent e = new FeedEvent(msg, 
				msg.timestamp,
				getEventRecipientForMessage(msg));
		
		e.feed = this; 
				
		return new FeedEvent[] {e};
	}

//	@Override
//	protected List<Class> getValidSubscriberClasses() {
//		return Arrays.asList(new Class[] {ConfigurableStreamModule.class});
//	}

//	@Override
//	protected Object getEventRecipientKey(Object subscriber) {
//		return ((KafkaModule)subscriber).getTopic();
//	}

//	@Override
//	protected IEventRecipient createEventRecipient(Object subscriber) {
//		ConfigurableStreamModule m = (ConfigurableStreamModule)subscriber;
//		return new MapMessageEventRecipient(globals,m.getStream());
//	}

}
