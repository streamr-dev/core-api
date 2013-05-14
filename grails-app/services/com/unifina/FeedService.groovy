package com.unifina

import com.unifina.data.Feed
import com.unifina.data.IFeed
import com.unifina.data.Stream
import com.unifina.feed.FeedFactory
import com.unifina.feed.MessageRecipient
import com.unifina.utils.Globals

class FeedService {

	String getFeedClass(Feed domain, boolean historical) {
		return historical ? domain.backtestFeed : domain.realtimeFeed
	}
	
    IFeed instantiateFeed(Feed domain, boolean historical, Globals globals) {
		String className = getFeedClass(domain,historical)
		IFeed feed = this.getClass().getClassLoader().loadClass(className).newInstance(globals)
		feed.setTimeZone(TimeZone.getTimeZone(domain.timezone))
		return feed
    }
	
	Stream getStream(Long id) {
		return Stream.get(id)
	}
	
	Stream getStream(String name) {
		return Stream.findByName(name)
	}
	
	Feed getFeed(Long id) {
		return Feed.get(id)
	}
	
	Feed getFeedByRealtimeClass(String className) {
		return Feed.findByRealtimeFeed(className)
	}
	
	MessageRecipient getMessageRecipient(Feed domain) {
		return FeedFactory.getInstance(domain)
	}
}
