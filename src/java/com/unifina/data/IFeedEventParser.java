package com.unifina.data;

import java.io.IOException;


public interface IFeedEventParser {
	public FeedEvent getNextEvent() throws IOException;
}
