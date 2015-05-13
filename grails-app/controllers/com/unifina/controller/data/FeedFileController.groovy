package com.unifina.controller.data

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.feed.file.AbstractFeedFileDiscoveryUtil
import com.unifina.service.FeedFileService

@Secured(["ROLE_ADMIN"])
class FeedFileController {

	FeedFileService feedFileService

	def discover() {
		List messages = []
		List feedIds = params.list("id")
		
		// Use ids given as params, or if none given, get all feeds
		List<Feed> feeds = (feedIds!=null && feedIds.size()>0 ? Feed.getAll(feedIds) : Feed.list())
		
		feeds.each {Feed feed->
			AbstractFeedFileDiscoveryUtil du = feedFileService.getDiscoveryUtil(feed)
			if (du) {
				int count = du.discover()
				messages << "Found $count new files for feed $feed.id"
			}
			else messages << "File discovery not enabled for feed $feed.id"
		}
		
		if (messages.size()>0)
			flash.message = messages.join("<br>")
			
		redirect(action:"index")
	}
	
	def preprocess() {
		FeedFile.findAllByProcessed(false).findAll {!it.processing && !it.processTaskCreated}.each {
			feedFileService.createPreprocessTask(it)
		}
		
		flash.message = "Preprocess tasks created."
		
		redirect(action:"index")
	}
	
	
}
