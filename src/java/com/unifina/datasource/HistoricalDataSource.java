package com.unifina.datasource;

import com.unifina.data.HistoricalEventQueue;
import com.unifina.feed.AbstractFeed;
import com.unifina.utils.Globals;

import java.util.Map;

public class HistoricalDataSource extends DataSource {

	private HistoricalEventQueue eventQueue;
	private final int speed;
	
	public HistoricalDataSource(Globals globals) {
		super(true, globals);
		speed = readSpeedConfiguration(globals);
	}

	@Override
	protected DataSourceEventQueue initEventQueue(Globals globals) {
		return eventQueue = new HistoricalEventQueue(globals, this);
	}

	@Override
	protected void onSubscribedToFeed(AbstractFeed feed) {
		eventQueue.addFeed(feed);
	}

	@Override
	protected void doStartFeed() throws Exception {
		eventQueue.setSpeed(speed);
		eventQueue.start();
	}
	
	@Override
	protected void doStopFeed() throws Exception {
		eventQueue.abort();
	}

	private static int readSpeedConfiguration(Globals globals) {
		Map ctx = globals.getSignalPathContext();
		return ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0;
	}
}
