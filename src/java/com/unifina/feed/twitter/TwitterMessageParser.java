package com.unifina.feed.twitter;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.unifina.feed.MessageParser;

public class TwitterMessageParser implements MessageParser<Object, Status> {
	@Override
	public Status parse(Object raw) {
		if (raw instanceof Status)
			return ((Status)raw);
		else {
			try {
				return TwitterObjectFactory.createStatus(raw.toString());
			} catch (TwitterException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
