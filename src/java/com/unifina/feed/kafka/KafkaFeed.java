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

}
