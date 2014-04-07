package com.unifina.feed.file;

import java.net.URL;
import java.util.Date;
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
		List<Date> days = discoverDays();
		int counter = 0;
		for (Date day : days) {
			if (feedFileService.getFeedFile(day, feed)==null) {
				// New day found!
				handleNewDay(day);
				counter++;
			}
		}
		disconnect();
		return counter;
	}
	
	protected void handleNewDay(Date day) {
		feedFileService.createCreateAndPreprocessTask(getURL(day), day, feed);
	}

	protected abstract void connect();
	protected abstract List<Date> discoverDays();
	protected abstract URL getURL(Date day);
	protected abstract void disconnect();
	
}
