package com.unifina.feed.twitter;

import twitter4j.Status;

import com.unifina.data.FeedEvent;
import com.unifina.feed.AbstractEventRecipient;
import com.unifina.signalpath.twitter.TwitterModule;
import com.unifina.utils.Globals;

public class TwitterEventRecipient extends AbstractEventRecipient<TwitterModule> {

	public TwitterEventRecipient(Globals globals) {
		super(globals);
	}

	@Override
	protected void sendOutputFromModules(FeedEvent event) {
		Status s = (Status) event.content;
		for (TwitterModule m : modules) {
			m.tweet.send(s.getRetweetedStatus() != null ? s.getRetweetedStatus().getText() : s.getText());
			m.username.send(s.getUser().getScreenName());
			m.name.send(s.getUser().getName());
			m.language.send(s.getLang());
			m.followers.send(s.getUser().getFollowersCount());
			m.isRetweet.send(s.getRetweetedStatus() != null ? 1D : 0D);
			m.isReply.send(s.getInReplyToScreenName() != null ? 1D : 0D);
		}
	}
	
}
