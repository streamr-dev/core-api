package com.unifina.data;

import com.unifina.datasource.IDayListener;
import com.unifina.datasource.ITimeListener;

public interface IEventQueue {
	public void addTimeListener(ITimeListener timeListener);
	public void addDayListener(IDayListener dayListener);
	public void enqueue(FeedEvent event);
	public void start() throws Exception;
	public void abort();
	public boolean isEmpty();
}
