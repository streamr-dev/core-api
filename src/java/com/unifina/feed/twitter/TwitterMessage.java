package com.unifina.feed.twitter;

import com.unifina.feed.ITimestamped;

import java.util.Date;

/**
 * Streamr representation of Twitter message, parsed from twitter4j.Status
 */
public class TwitterMessage implements ITimestamped {
	public Date timestamp;

	// TODO: parse for realz
	public twitter4j.Status status;

	@Override
	public Date getTimestamp() {
		return timestamp;
	}
}
