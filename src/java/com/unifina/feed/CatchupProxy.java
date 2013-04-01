package com.unifina.feed;

import java.util.LinkedList;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventQueue;

/**
 * This class will just queue all incoming messages.
 * @author Henri
 *
 */
public class CatchupProxy extends AbstractFeedProxy {
	
	LinkedList<Object> queue = new LinkedList<Object>();
	
	public synchronized void receive(Object object) {
		queue.add(object);
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized Object poll() {
		return queue.poll();
	}

	@Override
	public FeedEvent[] getNextEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void process(Object msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	Catchup getCatchup(AbstractFeedProxy proxy) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
