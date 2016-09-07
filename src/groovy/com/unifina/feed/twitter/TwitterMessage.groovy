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
	public String quotedText
	public String[] quotedUrls
	public TwitterStreamConfig streamConfig

	// those keywords that were found in this message
	public List<String> matchedKeywords

	public static TwitterMessage fromStatus(Status s, Date t=null) {
		TwitterMessage msg = new TwitterMessage()
		msg.timestamp = t ?: new Date()
		msg.text = s.text
		msg.urls = s.URLEntities*.expandedURL
		msg.username = s.user.name
		msg.name = s.user.screenName
		msg.language = s.lang
		msg.followers = s.user.followersCount

		// It seems a tweet is only either retweet (without comments) or quote tweet (with added comments)
		// see https://support.twitter.com/articles/20169873
		if (s.quotedStatus) {
			msg.quotedText = s.quotedStatus.text
			msg.quotedUrls = s.quotedStatus.URLEntities*.expandedURL
		} else if (s.retweetedStatus) {
			msg.quotedText = s.retweetedStatus.text
			msg.quotedUrls = s.retweetedStatus.URLEntities*.expandedURL
		}
		return msg
	}

	@Override
	public Date getTimestamp() {
		return timestamp
	}

	@Override
	String toString() {
		String ret = text
		if (urls) {
			ret += "[" + urls.join(",") + "]"
		}
		if (quotedText) {
			ret += " => " + quotedText
			if (quotedUrls) {
				ret += "[" + quotedUrls.join(",") + "]"
			}
		}
		return ret
	}
}
