package com.unifina.controller.data

import grails.converters.JSON

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Module

class StreamController {

	def springSecurityService
	
	def search() {
		Set<Feed> allowedFeeds = springSecurityService.currentUser?.feeds ?: new HashSet<>()
		List<Stream> streams = []

		if (!allowedFeeds.isEmpty()) {
			streams = Stream.createCriteria().list {
				if (params.feed) {
					eq("feed", Feed.load(params.feed))
				}
				if (params.module) {
					createAlias( "feed", "feed" )
					eq("feed.module", Module.load(params.module))
				}
				like("name","%"+params.term+"%")
				'in'("feed",allowedFeeds)
				maxResults(10)
			}
		}
		
		render streams as JSON
	}

}
