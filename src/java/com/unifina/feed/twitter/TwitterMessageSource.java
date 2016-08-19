package com.unifina.feed.twitter;

import com.unifina.api.InvalidStateException;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractMessageSource;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.InvokerHelper;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;

public class TwitterMessageSource extends AbstractMessageSource<TwitterMessage, String> {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TwitterMessageSource.class);

	// streamID -> SubscribedUser mapping, inverse of SubscribedUser.streams
	private Map<String, SubscribedUser> userForStream = new HashMap<>();

	// maps "twitter user" (access token) to streams authenticated with that token
	//   streams will be muxed together in Twitter Streaming API (see updateTwitterStreamFor)
	private static class SubscribedUser implements StatusListener {
		public List<TwitterStreamConfig> streams;	// keywords are per-stream...
		public TwitterStream twitterStream;			// ...but combined into one twitter4j.TwitterStream per accessToken

		public TwitterMessageSource messageSource;

		private long counter = 0;

		@Override public void onStatus(Status status) {
			log.info("Twitter message from " + status.getUser().getName() + " : " + status.getText());

			if (streams.size() == 0) {
				log.error("Twitter status message was received after unsubscribing, dropping it.");
				return;
			}

			TwitterMessage msg = TwitterMessage.fromStatus(status);

			if (streams.size() == 1) {
				// only one stream -> forward all incoming tweets to it
				msg.streamConfig = streams.get(0);
				messageSource.forward(msg, msg.streamConfig.getStreamId(), counter++, false);
			} else {
				// find streams whose keywords are found within tweet, forward a copy to each of them ("demux")
				//   see "mux" in updateTwitterStreamFor method below
				String tweet = msg.toString();
				List<String> matches = new LinkedList<>();
				for (TwitterStreamConfig conf : streams) {
					for (String kw : conf.getKeywords()) {
						if (tweet.contains(kw)) {
							matches.add(kw);
						}
					}
					if (matches.size() > 0) {
						TwitterMessage msg2 = TwitterMessage.fromStatus(status);
						msg2.streamConfig = conf;
						msg2.matchedKeywords = matches;
						messageSource.forward(msg2, conf.getStreamId(), counter++, false);
						matches = new LinkedList<>();
					}
				}
			}
		}

		@Override public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
		@Override public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
		@Override public void onScrubGeo(long l, long l1) { }
		@Override public void onStallWarning(StallWarning stallWarning) { }
		@Override public void onException(Exception ex) {
			ex.printStackTrace();
		}
	};

	public TwitterMessageSource(Feed feed, Map<String,Object> config) {
		super(feed, config);
	}

	@Override
	public void subscribe(String key) {
		if (userForStream.containsKey(key)) {
			log.error("TwitterMessageSource.subscribe: Stream " + key + " is already subscribed!");
			return;
		}

		Stream stream = (Stream)InvokerHelper.invokeMethod(Stream.class, "get", key);
		if (stream == null) {
			throw new IllegalArgumentException("Stream " + key + " does not exist!");
		}

		TwitterStreamConfig streamConf = TwitterStreamConfig.forStream(stream);

		String accessToken = streamConf.getAccessToken();
		String accessTokenSecret = streamConf.getAccessTokenSecret();
		if (accessToken == null || accessTokenSecret == null) {
			throw new InvalidStateException("Stream " + key + " hasn't been signed into Twitter yet!");
		}

		SubscribedUser sub = null;
		for (SubscribedUser sub2 : userForStream.values()) {
			if (accessToken.equals(sub2.streams.get(0).getAccessToken())) {
				sub = sub2;
				break;
			}
		}
		if (sub == null) {
			sub = new SubscribedUser();
			sub.streams = new ArrayList<>();
			sub.messageSource = this;
			Configuration conf = new ConfigurationBuilder()
					.setDebugEnabled(true)
					.setOAuthConsumerKey(TwitterStreamConfig.consumerKey)
					.setOAuthConsumerSecret(TwitterStreamConfig.consumerSecret)
					.setOAuthAccessToken(accessToken)
					.setOAuthAccessTokenSecret(accessTokenSecret)
					.build();
			TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
			sub.twitterStream = twitterStream;
			twitterStream.addListener(sub);
		}

		sub.streams.add(streamConf);
		updateTwitterStreamFor(sub);

		userForStream.put(key, sub);
	}

	@Override
	public void unsubscribe(String key) {
		SubscribedUser sub = userForStream.get(key);
		if (sub == null) {
			log.error("TwitterMessageSource.unsubscribe: Stream " + key + " isn't subscribed!");
			return;
		}

		TwitterStreamConfig removed = null;
		for (int i = 0; i < sub.streams.size(); i++) {
			if (sub.streams.get(i).getStreamId().equals(key)) {
				removed = sub.streams.remove(i);
				break;
			}
		}
		if (removed == null) {
			throw new InvalidStateException("TwitterMessageSource.unsubscribe: stream wasn't found in SubscribedUser.streams");
		}
		updateTwitterStreamFor(sub);

		userForStream.remove(key);
	}

	@Override
	public void close() {
		log.info("Closing TwitterMessageSource, unsubscribing " + userForStream.size() + " streams.");
		String[] keys = userForStream.keySet().toArray(new String[userForStream.size()]);	// make a copy
		for (String key : keys) {
			unsubscribe(key);
		}
	}

	private void updateTwitterStreamFor(SubscribedUser sub) {
		// get union of keywords of streams this user has subscribed to ("mux", see "demux" in TwitterFeed.process)
		Set<String> keywords = new HashSet<>();
		for (TwitterStreamConfig stream : sub.streams) {
			keywords.addAll(stream.getKeywords());
		}

		if (keywords.size() < 1) {
			sub.twitterStream.cleanUp();
		} else {
			FilterQuery query = new FilterQuery(0, new long[0], keywords.toArray(new String[keywords.size()]));
			sub.twitterStream.filter(query);
		}
	}
}
