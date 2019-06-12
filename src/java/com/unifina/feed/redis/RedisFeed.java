package com.unifina.feed.redis;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.feed.map.MapMessageEventRecipient;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

public class RedisFeed extends AbstractFeedProxy<ConfigurableStreamModule, StreamMessage, StreamMessage, String, MapMessageEventRecipient> {

	public RedisFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

	@Override
	protected FeedEvent[] process(StreamMessage msg) {
		FeedEvent e = new FeedEvent(msg,
				msg.getTimestampAsDate(),
				getEventRecipientForMessage(msg));

		e.feed = this;

		return new FeedEvent[] {e};
	}

}
