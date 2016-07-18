package com.unifina.feed.twitter

import com.unifina.feed.ITimestamped
import groovy.transform.CompileStatic
import twitter4j.Status

import java.util.Date
import java.util.List

/**
 * Streamr representation of Twitter message, parsed from twitter4j.Status
 */
class TwitterMessage implements ITimestamped {
	public Date timestamp
	public String text
	public String[] urls
	public String username
	public String name
	public String language
	public int followers
	public boolean isRetweet
	public TwitterStreamConfig streamConfig

	public static TwitterMessage fromStatus(Status s, Date t=null) {
		TwitterMessage msg = new TwitterMessage()
		msg.timestamp = t ?: new Date()
		msg.text = s.text
		msg.urls = s.URLEntities*.expandedURL
		msg.username = s.user.name
		msg.name = s.user.screenName
		msg.language = s.lang		// TODO: or s.user.lang? s.lang is often "Und" (undefined)
		msg.followers = s.user.followersCount
		msg.isRetweet = s.retweet
		return msg
	}

	@Override
	public Date getTimestamp() {
		return timestamp
	}

	@Override
	String toString() {
		return text + "[" + urls.join(",") + "]"
	}
}
