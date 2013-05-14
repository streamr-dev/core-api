package com.unifina.data;

import java.io.IOException;


public interface IHistoricalEventSource {
	public FeedEvent getNextEvent() throws IOException;
}
