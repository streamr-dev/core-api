package com.unifina.feed.twitter;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractMessageSource;
import com.unifina.feed.MessageRecipient;

import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;

public class TwitterMessageSource extends AbstractMessageSource<Status, String> {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TwitterMessageSource.class);

	private MessageRecipient<Status, String> recipient;

	public final String topic = "twitter-stream-apikey";

	private StatusListener listener = new StatusListener() {
		private long counter = 0;

		@Override public void onStatus(Status status) {
			log.info("Twitter message from " + status.getUser().getName() + " : " + status.getText());
			forward(status, "twitter", counter++, false);
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

		TwitterStreamConfig streamConf = new TwitterStreamConfig();
		streamConf.setAccessToken("750594680841207808-08ws31Jwp44guWd3uh8YRYSslbdQ1Lp");
		streamConf.setAccessTokenSecret("DNY6zU2FeZRyyRVqVmK2Vp4fHUPFlAsZUNKbKNgmFUgs0");

		Configuration conf = new ConfigurationBuilder()
				.setDebugEnabled(true)
				.setOAuthConsumerKey(TwitterStreamConfig.consumerKey)
				.setOAuthConsumerSecret(TwitterStreamConfig.consumerSecret)
				.setOAuthAccessToken(streamConf.getAccessToken())
				.setOAuthAccessTokenSecret(streamConf.getAccessTokenSecret())
				.build();

		TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
		twitterStream.addListener(listener);

		// TODO: start filtering in subscribe(), update the filter query
		FilterQuery query = new FilterQuery(0, new long[0], new String[] { "yolo" });
		twitterStream.filter(query);
	}

	@Override
	public void close() {

	}

	@Override
	public void subscribe(String key) {

	}

	@Override
	public void unsubscribe(String key) {

	}
}
