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
		TwitterMessage msg = event.content;

		for (TwitterModule m : modules) {
			m.tweet.send(msg.text);
			m.username.send(msg.username);
			m.name.send(msg.name);
			m.language.send(msg.language);
			m.followers.send(msg.followers);
			m.isRetweet.send(msg.isRetweet);
		}
	}

}
