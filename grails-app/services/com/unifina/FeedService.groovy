package com.unifina

import com.unifina.data.Feed
import com.unifina.data.IFeed
import com.unifina.data.Stream
import com.unifina.feed.FeedFactory
import com.unifina.feed.FeedNotFoundException
import com.unifina.feed.MessageRecipient
import com.unifina.feed.StreamNotFoundException
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
		Stream result = Stream.get(id)
		if (!result)
			throw new StreamNotFoundException(id)
		else return result
	}
	
	Stream getStream(String name) {
		Stream result = Stream.findByName(name)
		if (!result)
			throw new StreamNotFoundException(name)
		else return result
	}
	
	Feed getFeed(Long id) {
		Feed result = Feed.get(id)
		if (!result)
			throw new FeedNotFoundException(id)
		else return result
	}
	
	Feed getFeedByRealtimeClass(String className) {
		Feed result = Feed.findByRealtimeFeed(className)
		if (!result)
			throw new FeedNotFoundException(className)
		else return result
	}
	
	MessageRecipient getMessageRecipient(Feed domain) {
		return FeedFactory.getInstance(domain)
	}
}
