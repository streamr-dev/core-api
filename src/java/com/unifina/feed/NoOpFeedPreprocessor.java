package com.unifina.feed;

import java.io.InputStream;
import java.util.List;

import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.service.FeedFileService;
import com.unifina.service.FeedService;

/**
 * This preprocessor can be assigned to feeds that require no preprocessing.
 * The preprocess method does nothing.
 */
public class NoOpFeedPreprocessor extends AbstractFeedPreprocessor {
	
	FeedService feedService = null;
	
	@Override
	public void preprocess(FeedFile feedFile, FeedFileService feedFileService, FeedService feedService, InputStream inputStream, boolean isCompressed, boolean saveToDiskFirst) {
		this.feedService = feedService;
		this.feedFile = feedFile;
		
		// Do nothing
	}

	/**
	 * Returns null
	 */
	@Override
	public String getPreprocessedFileName(String name, Stream stream,
			boolean compressed) {
		return name;
	}

	@Override
	protected void preprocess(InputStream inputStream, String name) {
		// Never called because the public method that calls this method is overridden
	}

	/**
	 * Returns the list of Streams manually defined for this Feed
	 */
	@Override
	public List<Stream> getFoundStreams() {
		return feedService.getStreams(feedFile.getFeed());
	}
	
}
