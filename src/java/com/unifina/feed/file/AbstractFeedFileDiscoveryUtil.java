package com.unifina.feed.file;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.data.Feed;
import com.unifina.service.FeedFileService;

public abstract class AbstractFeedFileDiscoveryUtil {
	
	protected GrailsApplication grailsApplication;
	protected Feed feed;
	protected FeedFileService feedFileService;
	protected Map<String, Object> config;
	
	public AbstractFeedFileDiscoveryUtil(GrailsApplication grailsApplication, Feed feed, Map<String,Object> config) {
		this.grailsApplication = grailsApplication;
		this.feed = feed;
		this.feedFileService = (FeedFileService) grailsApplication.getMainContext().getBean("feedFileService");
		this.config = config;
	}
	
	public int discover() {
		connect();
		List<RemoteFeedFile> files = discoverFiles();
		int counter = 0;
		for (RemoteFeedFile file : files) {
			if (feedFileService.getFeedFile(file)==null) {
				// New file found!
				handleNewFile(file);
				counter++;
			}
		}
		disconnect();
		return counter;
	}
	
	protected void handleNewFile(RemoteFeedFile file) {
		feedFileService.createCreateAndPreprocessTask(file);
	}

	protected abstract void connect();
	protected abstract List<RemoteFeedFile> discoverFiles();
//	protected abstract URL getURL(Date day);
	protected abstract void disconnect();
}
