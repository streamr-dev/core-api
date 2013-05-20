package com.unifina.datasource;

import java.util.Map;

import org.apache.log4j.Logger;

import com.unifina.data.BacktestEventQueue;
import com.unifina.data.IFeed;
import com.unifina.data.IRequireFeed;
import com.unifina.utils.Globals;

public class BacktestDataSource extends DataSource {
	
//	private static final Logger log = Logger.getLogger(BacktestDataSource.class);
	
	public BacktestDataSource(Globals globals) {
		super(true,globals);
	}

	@Override
	protected DataSourceEventQueue initEventQueue() {
		return new BacktestEventQueue(globals, this);
	}
	
	@Override
	protected IFeed registerModule(IRequireFeed o) {
		IFeed feed = super.registerModule(o);
		
		if (eventQueue instanceof BacktestEventQueue)
			((BacktestEventQueue)eventQueue).addFeed(feed);
		else throw new IllegalStateException("eventQueue is not an instance of BacktestEventQueue!");
		
		return feed;
	}
	
	@Override
	protected void doStartFeed() throws Exception {
		@SuppressWarnings("rawtypes")
		Map ctx = globals.getSignalPathContext();
		
		final int speed = (ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0);
		
		if (eventQueue instanceof BacktestEventQueue) {
			((BacktestEventQueue)eventQueue).setSpeed(speed);
			eventQueue.start();
			// TODO: clear?
		}
		else throw new IllegalStateException("eventQueue is not an instance of BacktestEventQueue!");

	}
	
	@Override
	protected void doStopFeed() throws Exception {
		globals.abort = true;
		eventQueue.abort();
//		globals.market.disconnect()
	}
	
}
