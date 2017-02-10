package com.unifina.feed.redis;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.feed.map.MapMessage;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

public class RedisFeed extends AbstractFeedProxy<ConfigurableStreamModule, StreamrBinaryMessageWithKafkaMetadata, MapMessage, String, MapMessageEventRecipient> {

	public RedisFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent[] process(MapMessage msg) {
		FeedEvent e = new FeedEvent(msg, 
				msg.timestamp,
				getEventRecipientForMessage(msg));
		
		e.feed = this; 
				
		return new FeedEvent[] {e};
	}

}
