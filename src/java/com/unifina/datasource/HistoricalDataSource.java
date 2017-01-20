package com.unifina.datasource;

import java.util.Map;

import com.unifina.data.HistoricalEventQueue;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeed;
import com.unifina.utils.Globals;

public class HistoricalDataSource extends DataSource {
	
	public HistoricalDataSource(Globals globals) {
		super(true,globals);
	}

	@Override
	protected DataSourceEventQueue initEventQueue() {
		return new HistoricalEventQueue(globals, this);
	}
	
	@Override
	protected AbstractFeed subscribeToFeed(Object subscriber, Feed feedDomain) {
		AbstractFeed feed = super.subscribeToFeed(subscriber, feedDomain);
		
		if (eventQueue instanceof HistoricalEventQueue)
			((HistoricalEventQueue)eventQueue).addFeed(feed);
		else throw new IllegalStateException("eventQueue is not an instance of HistoricalEventQueue!");
		
		return feed;
	}
	
	@Override
	protected void doStartFeed() throws Exception {
		@SuppressWarnings("rawtypes")
		Map ctx = globals.getSignalPathContext();
		
		final int speed = (ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0);
		
		if (eventQueue instanceof HistoricalEventQueue) {
			((HistoricalEventQueue)eventQueue).setSpeed(speed);
			eventQueue.start();
			// TODO: clear?
		}
		else throw new IllegalStateException("eventQueue is not an instance of HistoricalEventQueue!");

	}
	
	@Override
	protected void doStopFeed() throws Exception {
		eventQueue.abort();
	}
	
}
