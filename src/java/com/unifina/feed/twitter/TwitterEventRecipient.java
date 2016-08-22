package com.unifina.feed.twitter;

import java.util.Map;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Stream;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.feed.kafka.KafkaMessage;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;

public class TwitterEventRecipient extends StreamEventRecipient<TwitterModule, TwitterMessage> {

	public TwitterEventRecipient(Globals globals, Stream stream) {
		super(globals, stream);
	}

	@Override
	protected void sendOutputFromModules(FeedEvent<TwitterMessage, ? extends IEventRecipient> event) {
		for (TwitterModule m : modules) {
			m.forward(event.content);
		}
	}

}
