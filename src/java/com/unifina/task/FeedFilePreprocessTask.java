package com.unifina.task;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.data.FeedFile;
import com.unifina.domain.task.Task;
import com.unifina.feed.AbstractFeedPreprocessor;
import com.unifina.service.FeedFileService;
import com.unifina.service.FeedService;

public class FeedFilePreprocessTask extends AbstractTask {

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
		feedFile = feedFileService.getFeedFile((long) config.get("feedFileId"));
		preprocessor = feedService.instantiatePreprocessor(feedFile.getFeed());
	}

	@Override
	public boolean run() {
		preprocessor.preprocess(feedFile, feedFileService);
		return true;
	}

	@Override
	public void onComplete() {
		
	}

}
