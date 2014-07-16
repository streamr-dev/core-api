package com.unifina.task

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractFeedPreprocessor
import com.unifina.feed.file.FileStorageAdapter
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
		// Get InputStream from location specified in the config
		if (!config.location)
			throw new RuntimeException("Task config does not define the location!")
		if (!config.beginDate)
			throw new RuntimeException("Task config does not define the beginDate!")
		if (!config.endDate)
			throw new RuntimeException("Task config does not define the endDate!")
		if (!config.name)
			throw new RuntimeException("Task config does not define the name!")
		if (!config.feedId)
			throw new RuntimeException("Task config does not define the feedId!")
			
		FileStorageAdapter adapter = null
		if (config.fileStorageAdapter) {
			log.info("Creating FileStorageAdapter: $config.fileStorageAdapter")
			adapter = feedFileService.getFileStorageAdapter(config.fileStorageAdapter)
		}

		RemoteFeedFile remoteFile = new RemoteFeedFile(config.name.toString(), new Date((long)config.beginDate), new Date((long)config.endDate), Feed.get(config.feedId), config.location, adapter?.getClass())
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
		
		/**
		 * If the config defines a file storage adapter, use it to retrieve the InputStream.
		 * Otherwise, try to use open the location as an URL.
		 */
		InputStream inputStream = (adapter ? adapter.retrieve(remoteFile.location) : new URL(remoteFile.location).openConnection().getInputStream())
		
		preprocessor.preprocess(feedFile, feedFileService, inputStream, remoteFile.getName().endsWith(".gz"), false);
			
		return true;
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
}
