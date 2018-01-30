package com.unifina.datasource;

import com.unifina.data.HistoricalEventQueue;
import com.unifina.feed.AbstractFeed;
import com.unifina.utils.Globals;

public class HistoricalDataSource extends DataSource {

	private final HistoricalEventQueue eventQueue;
	
	public HistoricalDataSource(Globals globals) {
		super(true, globals);
		eventQueue = new HistoricalEventQueue(globals, this);
	}

	@Override
	protected DataSourceEventQueue getEventQueue() {
		return eventQueue;
	}

	@Override
	protected void onSubscribedToFeed(AbstractFeed feed) {
		eventQueue.addFeed(feed);
	}

	@Override
	protected void doStartFeed() throws Exception {
		eventQueue.start();
	}
	
	@Override
	protected void doStopFeed() throws Exception {
		eventQueue.abort();
	}
}
