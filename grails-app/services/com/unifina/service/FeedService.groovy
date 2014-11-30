package com.unifina.service

import com.unifina.data.IFeed
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.feed.AbstractFeedPreprocessor
import com.unifina.feed.FeedFactory
import com.unifina.feed.FeedNotFoundException
import com.unifina.feed.MessageRecipient
import com.unifina.feed.StreamNotFoundException
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.Globals

class FeedService {

	def grailsApplication
	
	String getFeedClass(Feed domain, boolean historical) {
		return historical ? domain.backtestFeed : domain.realtimeFeed
	}
	
    IFeed instantiateFeed(Feed domain, boolean historical, Globals globals) {
		String className = getFeedClass(domain,historical)
		IFeed feed = this.getClass().getClassLoader().loadClass(className).newInstance(globals,domain)
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
	
	Stream getStreamByFeedAndLocalId(Feed feed, String localId) {
		Stream result = Stream.findByFeedAndLocalId(feed, localId)
		if (!result)
			throw new StreamNotFoundException("Feed: $feed.name, LocalId: $localId")
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
	
	Feed getFeedByModule(AbstractSignalPathModule m) {
		Feed result = Feed.createCriteria().get() {
			module {
				eq("implementingClass", m.getClass().getName())
			}
		}
		
		if (!result)
			throw new FeedNotFoundException("For module "+m)
		else return result
	}
	
	MessageRecipient getMessageRecipient(Feed domain) {
		return FeedFactory.getInstance(domain, grailsApplication.config)
	}

	List<Stream> getStreams(Feed feed) {
		return Stream.findAllByFeed(feed)
	}
}
