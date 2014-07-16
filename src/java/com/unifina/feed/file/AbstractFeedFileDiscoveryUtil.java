package com.unifina.feed.file;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.data.Feed;
import com.unifina.service.FeedFileService;

/**
 * Common abstract class for all feed file discovery utils. A call to discover()
 * will create FeedFileCreateAndPreprocessTasks for all new FeedFiles found in
 * the location represented by the specific implementation of this class.
 */
public abstract class AbstractFeedFileDiscoveryUtil {
	
	protected GrailsApplication grailsApplication;
	protected Feed feed;
	protected FeedFileService feedFileService;
	protected Map<String, Object> config;
	
	Pattern pattern = null;
	
	public AbstractFeedFileDiscoveryUtil(GrailsApplication grailsApplication, Feed feed, Map<String,Object> config) {
		this.grailsApplication = grailsApplication;
		this.feed = feed;
		this.feedFileService = (FeedFileService) grailsApplication.getMainContext().getBean("feedFileService");
		this.config = config;
		
		if (config.containsKey("pattern"))
			pattern = Pattern.compile((String) config.get("pattern"));
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
	
	protected List<RemoteFeedFile> discoverFiles() {
		// List all files at location
		List<String> files = listFiles();
		
		// Match URI against the pattern
		List<String> matchedFiles = new ArrayList<>();
		for (String location : files) {
			if (pattern.matcher(location.toString()).find())
				matchedFiles.add(location);
		}
		
		// Create RemoteFeedFiles from filtered list
		List <RemoteFeedFile> result = new ArrayList<>();
		for (String location : matchedFiles) {
			result.add(createRemoteFeedFile(location));
		}
		
		return result;
	}
	
	protected void handleNewFile(RemoteFeedFile file) {
		feedFileService.createCreateAndPreprocessTask(file);
	}
	
	protected RemoteFeedFile createRemoteFeedFile(String location) {
		// Parse date from url
		Date beginDate = getBeginDate(location);
		Date endDate = getEndDate(location);
		
		return new RemoteFeedFile(FilenameUtils.getName(location), beginDate, endDate, feed, location, null);
	}
	
	
	/**
	 * List all file locations in remote target
	 */
	protected abstract List<String> listFiles();
	
	/**
	 * Connect to remote location, called before listFiles()
	 */
	protected abstract void connect();
	
	/**
	 * Disconnect from remote location, called in the end
	 */
	protected abstract void disconnect();
	
	/**
	 * Extract the begin timestamp of data from the file at given location
	 */
	protected abstract Date getBeginDate(String location);
	
	/**
	 * Extract the end timestamp of data from the file at given location
	 */
	protected abstract Date getEndDate(String location);
	
}
