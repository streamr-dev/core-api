package com.unifina.feed;

import com.unifina.ItchFile;
import com.unifina.data.Stream;

public interface IFeedPreprocessor {
	
	/**
	 * Preprocesses a feed file, producing separate stream files.
	 * @param file
	 * @return
	 */
	public boolean preprocess(ItchFile file);
	
	public String getPreprocessedFileName(ItchFile file, Stream stream);
	
}
