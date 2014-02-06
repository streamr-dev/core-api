package com.unifina.feed;

import com.unifina.domain.ItchFile;
import com.unifina.domain.data.Stream;

public interface IFeedPreprocessor {
	
	/**
	 * Preprocesses a feed file, producing separate stream files.
	 * @param file
	 * @return
	 */
	public boolean preprocess(ItchFile file);
	
	public String getPreprocessedFileName(ItchFile file, Stream stream);
	
}
