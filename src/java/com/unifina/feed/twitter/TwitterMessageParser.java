package com.unifina.feed.twitter;

import com.unifina.feed.MessageParser;

public class TwitterMessageParser implements MessageParser<TwitterMessage, TwitterMessage> {
	@Override
	public TwitterMessage parse(TwitterMessage msg) {
		return msg;
	}
}
