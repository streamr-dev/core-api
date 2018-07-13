package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.exceptions.StreamNotFoundException
import com.unifina.feed.AbstractFeed
import com.unifina.feed.DataRangeProvider
import com.unifina.utils.Globals
import groovy.transform.CompileStatic

class FeedService {

	def grailsApplication
	def permissionService
	
	@CompileStatic
	String getFeedClass(Feed domain, boolean historical) {
		return historical ? domain.backtestFeed : domain.realtimeFeed
	}
	
	@CompileStatic
    AbstractFeed instantiateFeed(Feed domain, boolean historical, Globals globals) {
		String className = getFeedClass(domain,historical)
		AbstractFeed feed = (AbstractFeed) this.getClass().getClassLoader().loadClass(className).newInstance(globals,domain)
		feed.setTimeZone(TimeZone.getTimeZone(domain.timezone))
		return feed
    }

	@CompileStatic
	Stream getStream(String id) {
		Stream result = Stream.get(id)
		if (!result) {
			throw new StreamNotFoundException(id)
		}
		return result
	}

	DataRangeProvider instantiateDataRangeProvider(Feed feed) {
		if (feed?.dataRangeProviderClass == null) {
			return null
		} else {
			Class clazz = getClass().getClassLoader().loadClass(feed.dataRangeProviderClass)
			return clazz.newInstance()
		}
	}
}
