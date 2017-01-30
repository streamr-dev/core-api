package com.unifina.feed.twitter;

import com.unifina.feed.AbstractStreamrMessage;
import twitter4j.Status;

import java.util.*;

/**
 * Streamr representation of Twitter message, parsed from twitter4j.Status
 */
class TwitterMessage extends AbstractStreamrMessage {
	// those keywords that were found in this message
	private List<String> matchedKeywords;

	private static Set<String> keySet = new HashSet<>(Arrays.asList(new String[] {
			"tweet",
			"username",
			"name",
			"language",
			"followers",
			"retweet?",
			"keywords"
	}));

	private final Status status;

	public TwitterMessage(String streamId, int partition, Date timestamp, Date receiveTime, Status status, List<String> matchedKeywords) {
		super(streamId, partition, timestamp, receiveTime);
		this.status = status;
		this.matchedKeywords = matchedKeywords;
	}

	@Override
	public Set<String> keySet() {
		return keySet;
	}

	@Override
	public Object get(String key) {
		switch (key) {
			case "tweet":
				return status.getText();
			case "username":
				return status.getUser().getScreenName();
			case "name":
				return status.getUser().getName();
			case "language":
				return status.getLang();
			case "followers":
				return status.getUser().getFollowersCount();
			case "retweet?":
				return status.getQuotedStatus() != null || status.getRetweetedStatus() != null;
			case "keywords":
				return matchedKeywords;
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		return status.toString();
	}
}
