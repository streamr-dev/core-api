package com.unifina.feed.twitter;

import com.unifina.api.InvalidStateException;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import com.unifina.feed.AbstractMessageSource;

import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;

public class TwitterMessageSource extends AbstractMessageSource<Status, String> {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TwitterMessageSource.class);

	// streamID -> SubscribedUser mapping, inverse of SubscribedUser.streams
	private Map<String, SubscribedUser> userForStream = new HashMap<>();

	private static class SubscribedUser implements StatusListener {
		public SecUser user;						// one access token per SecUser
		public List<TwitterStreamConfig> streams;	// keywords are per-stream...
		public TwitterStream twitterStream;			// ...but combined into one twitter4j.TwitterStream per user

		public TwitterMessageSource messageSource;

		private long counter = 0;

		@Override public void onStatus(Status status) {
			log.info("Twitter message from " + status.getUser().getName() + " : " + status.getText());

			if (streams.size() < 1) {
				log.error("Twitter status message " + status.getText() + " received after unsubscribing.");
				return;
			}

			// only one stream -> forward all to it
			if (streams.size() < 2) {
				messageSource.forward(status, streams.get(0).getStream().getId(), counter++, false);
			} else {
				// find streams whose keywords match to status, forward to each of them ("demux", see "mux" in updateTwitterStreamFor)
				String tweet = status.getText();    // TODO: expand shortened URLs for matching
				for (TwitterStreamConfig stream : streams) {
					for (String kw : stream.getKeywords()) {
						if (tweet.contains(kw)) {
							messageSource.forward(status, stream.getStream().getId(), counter++, false);
							break;
						}
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
	public void close() {
		log.info("Closing TwitterMessageSource, unsubscribing " + userForStream.size() + " streams.");
		for (String key : userForStream.keySet()) {
			unsubscribe(key);
		}
	}

	@Override
	public void subscribe(String key) {
		if (userForStream.containsKey(key)) {
			log.error("TwitterMessageSource.subscribe: Stream " + key + " is already subscribed!");
			return;
		}

		Stream stream = (Stream)Stream.get(key);
		if (stream == null) {
			throw new IllegalArgumentException("Stream " + key + " does not exist!");
		}

		TwitterStreamConfig streamConf = TwitterStreamConfig.fromStream(stream);

		String accessToken = streamConf.getAccessToken();
		String accessTokenSecret = streamConf.getAccessTokenSecret();
		if (accessToken == null || accessTokenSecret == null) {
			throw new InvalidStateException("Stream " + key + " hasn't been signed into Twitter yet!");
		}

		SubscribedUser sub = userForStream.get(key);
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
			if (sub.streams.get(i).getStream().getId().equals(key)) {
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

	private void updateTwitterStreamFor(SubscribedUser sub) {
		if (sub.streams.size() < 1) {
			sub.twitterStream.cleanUp();
			return;
		}

		// get union of keywords of streams this user has subscribed ("mux", see "demux" in SubscribedUser.onStatus)
		Set<String> keywords = new HashSet<>();
		for (TwitterStreamConfig stream : sub.streams) {
			keywords.addAll(stream.getKeywords());
		}

		FilterQuery query = new FilterQuery(0, new long[0], keywords.toArray(new String[keywords.size()]));
		sub.twitterStream.filter(query);
	}
}
