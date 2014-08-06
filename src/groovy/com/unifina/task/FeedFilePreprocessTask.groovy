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

/**
 * This is a task for preprocessing of files containing feed data.
 * A FeedFile entry is created for each file. The file is then
 * preprocessed by the AbstractFeedPreprocessor defined by the Feed
 * declaration.
 */
class FeedFilePreprocessTask extends AbstractTask {
	
	FeedFileService feedFileService;
	FeedService feedService;
	FeedFile feedFile;
	AbstractFeedPreprocessor preprocessor;
	
	private static final Logger log = Logger.getLogger(FeedFilePreprocessTask.class);
	
	public FeedFilePreprocessTask(Task task, Map<String, Object> config,
			GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		feedFileService = (FeedFileService) grailsApplication.getMainContext().getBean("feedFileService");
		feedService = (FeedService) grailsApplication.getMainContext().getBean("feedService");
	}
	
	@Override
	public boolean run() {
		// Check that the config contains the remoteFile config
		if (!config.remoteFile)
			throw new RuntimeException("Task config does not define the remoteFile!")

		// Instantiate the optional FileStorageAdapter if configured
		FileStorageAdapter adapter = null
		if (config.remoteFile.fileStorageAdapter) {
			log.info("Creating FileStorageAdapter: $config.remoteFile.fileStorageAdapter")
			adapter = feedFileService.getFileStorageAdapter(config.remoteFile.fileStorageAdapter)
		}

		// Instantiate the RemoteFeedFile from config.remoteFeedFile
		RemoteFeedFile remoteFile = new RemoteFeedFile(config.remoteFile.name.toString(), new Date((long)config.remoteFile.beginDate), new Date((long)config.remoteFile.endDate), Feed.get(config.remoteFile.feedId), config.remoteFile.location, config.remoteFile.compressed, adapter?.getClass())
		feedFile = feedFileService.getFeedFile(remoteFile)
		
		if (feedFile?.processed) {
			log.error("FeedFile $feedFile already exists and is processed!")
			return false
		}
		
		// If the FeedFile entry already exists, delete it and start from scratch
		if (feedFile) {
			log.info("Unprocessed FeedFile already exists. Deleting it and creating a new entry...")
			feedFile.delete()
		}
		
		FeedFile.withTransaction {
			feedFile = feedFileService.createFeedFile(remoteFile)
			feedFile.save(flush:true, failOnError:true)
		}
		
		/**
		 * If the config defines a file storage adapter, use it to retrieve the InputStream.
		 * Otherwise, try to use open the location as an URL.
		 */
		InputStream inputStream = (adapter ? adapter.retrieve(remoteFile.location) : new URL(remoteFile.location).openConnection().getInputStream())
		
		// Instantiate the preprocessor and start preprocessing
		preprocessor = feedService.instantiatePreprocessor(remoteFile.getFeed());
		preprocessor.preprocess(feedFile, feedFileService, feedService, inputStream, remoteFile.getName().endsWith(".gz"), false);

		// Update the Streams detected in the file
		feedFileService.saveOrUpdateStreams(preprocessor.getFoundStreams(), feedFile);
		
		// Mark the file as preprocessed
		feedFileService.setPreprocessed(feedFile);
		
		inputStream.close()
		
		return true;
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(RemoteFeedFile remoteFeedFile, boolean download) {
		return [remoteFile: remoteFeedFile.getConfig(), download:download]
	}
	
}
