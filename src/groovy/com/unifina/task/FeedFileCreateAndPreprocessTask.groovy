package com.unifina.task

import java.text.SimpleDateFormat

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractFeedPreprocessor
import com.unifina.service.FeedFileService
import com.unifina.service.FeedService
import com.unifina.service.FeedFileService.StreamResponse
import com.unifina.utils.TimeOfDayUtil;


class FeedFileCreateAndPreprocessTask extends AbstractTask {
	
	FeedFileService feedFileService;
	FeedService feedService;
	FeedFile feedFile;
	AbstractFeedPreprocessor preprocessor;
	
	private static final Logger log = Logger.getLogger(FeedFileCreateAndPreprocessTask.class);
	
	public FeedFileCreateAndPreprocessTask(Task task, Map<String, Object> config,
			GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		feedFileService = (FeedFileService) grailsApplication.getMainContext().getBean("feedFileService");
		feedService = (FeedService) grailsApplication.getMainContext().getBean("feedService");
	}
	
	@Override
	public boolean run() {
		// Get InputStream from URL as specified in the config
		if (!config.url)
			throw new RuntimeException("Task config does not define the url!")
		if (!config.day)
			throw new RuntimeException("Task config does not define the day!")
		if (!config.feedId)
			throw new RuntimeException("Task config does not define the feedId!")
			
		URL url = new URL(config.url.toString())
		Date day = new SimpleDateFormat("yyyy-MM-dd").parse(config.day)
		Feed feed = Feed.get((long)config.feedId)

		feedFile = FeedFile.findByFeedAndDay(feed, day)
		preprocessor = feedService.instantiatePreprocessor(feed);
		
		if (feedFile?.processed) {
			log.error("FeedFile $feedFile already exists and is processed!")
			return false
		}
		
		// If the FeedFile entry already exists, delete it
		if (feedFile) {
			log.info("Unprocessed FeedFile already exists. Deleting it and creating a new entry...")
			feedFile.delete()
		}
		
		FeedFile.withTransaction {
			feedFile = new FeedFile()
			feedFile.name = url.toString().substring(url.toString().lastIndexOf("/")+1)
			feedFile.day = day
			feedFile.processed = false
			feedFile.processing = true
			feedFile.processTaskCreated = true
			feedFile.feed = feed
			feedFile.save(flush:true, failOnError:true)
		}
		
		// Open the URL connection and preprocess on the fly
		URLConnection urlConnection = url.openConnection();
		preprocessor.preprocess(feedFile, feedFileService, urlConnection.getInputStream(), url.toString().endsWith(".gz"), false);
			
		return true;
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(URL url, Date day, Feed feed) {
		Map<String,Object> map = new LinkedHashMap<>(3);
		map.put("url", url.toString());
		map.put("day", new SimpleDateFormat("yyyy-MM-dd").format(TimeOfDayUtil.getMidnight(day)));
		map.put("feedId", feed.id);
		return map;
	}
}
