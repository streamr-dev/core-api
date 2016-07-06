package com.unifina.feed.twitter;

import com.unifina.feed.MessageParser;
import twitter4j.Status;

import java.util.Date;

public class TwitterMessageParser implements MessageParser<Status, TwitterMessage> {
	@Override
	public TwitterMessage parse(Status raw) {
		TwitterMessage msg = new TwitterMessage();
		msg.timestamp = new Date();
		msg.status = raw;
		return msg;
	}
}
