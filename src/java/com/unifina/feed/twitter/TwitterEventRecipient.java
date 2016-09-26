package com.unifina.feed.twitter;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Stream;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.feed.StreamrMessage;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;

import java.util.Map;
import java.util.Set;

public class TwitterEventRecipient extends StreamEventRecipient<TwitterModule, StreamrMessage> {

	public TwitterEventRecipient(Globals globals, Stream stream, Set<Integer> partitions) {
		super(globals, stream, partitions);
	}

	@Override
	protected void sendOutputFromModules(FeedEvent<StreamrMessage, ? extends IEventRecipient> event) {
		Map msg = event.content.payload;
		
		String tweet = (msg.containsKey("retweeted_status") ? MapTraversal.getString(msg, "retweeted_status.text") : MapTraversal.getString(msg, "text"));
		String username = MapTraversal.getString(msg, "user.screen_name");
		String name = MapTraversal.getString(msg, "user.name");
		String language = MapTraversal.getString(msg, "lang");
		Integer followers = MapTraversal.getInteger(msg, "user.followers_count");
		
		for (TwitterModule m : modules) {
			m.tweet.send(tweet);
			m.username.send(username);
			m.name.send(name);
			m.language.send(language);
			m.followers.send(followers);
			m.isRetweet.send(msg.containsKey("retweeted_status") ? 1D : 0D);
			m.isReply.send(msg.containsKey("in_reply_to_screen_name") ? 1D : 0D);
		}
	}

}
