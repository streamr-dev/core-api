package com.unifina.task

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractFeedPreprocessor
import com.unifina.feed.file.RemoteFeedFile
import com.unifina.service.FeedFileService
import com.unifina.service.FeedService



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
		if (!config.beginDate)
			throw new RuntimeException("Task config does not define the beginDate!")
		if (!config.endDate)
			throw new RuntimeException("Task config does not define the endDate!")
		if (!config.name)
			throw new RuntimeException("Task config does not define the name!")
		if (!config.feedId)
			throw new RuntimeException("Task config does not define the feedId!")
			
		RemoteFeedFile remoteFile = new RemoteFeedFile(config.name.toString(), new Date((long)config.beginDate), new Date((long)config.endDate), Feed.get(config.feedId), new URL(config.url))
		feedFile = feedFileService.getFeedFile(remoteFile)
		preprocessor = feedService.instantiatePreprocessor(remoteFile.getFeed());
		
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
			feedFile.name = remoteFile.getName()
//			feedFile.name = url.toString().substring(url.toString().lastIndexOf("/")+1)
			
			feedFile.beginDate = remoteFile.getBeginDate()
			feedFile.endDate = remoteFile.getEndDate()
			// TODO: remove deprecated
			feedFile.day = remoteFile.getBeginDate()
			
			feedFile.processed = false
			feedFile.processing = true
			feedFile.processTaskCreated = true
			feedFile.feed = remoteFile.getFeed()
			feedFile.save(flush:true, failOnError:true)
		}
		
		// Open the URL connection and preprocess on the fly
		URLConnection urlConnection = remoteFile.getUrl().openConnection();
		preprocessor.preprocess(feedFile, feedFileService, urlConnection.getInputStream(), remoteFile.getName().endsWith(".gz"), false);
			
		return true;
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
}
