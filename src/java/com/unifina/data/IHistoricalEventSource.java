package com.unifina.data;

import java.io.IOException;

@Deprecated
public interface IHistoricalEventSource {
	public FeedEvent next() throws IOException;
	public void close() throws IOException;
}
