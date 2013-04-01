package com.unifina.feed;


/**
 * Acts as a feed instance in a realtime situation. Really gets its messages
 * from an underlying singleton feed.
 * @author Henri
 *
 */
public abstract class AbstractFeedProxy extends Thread implements ICatchupFeed {
	Catchup catchup;
	
	@Override
	public boolean startCatchup() {
		CatchupProxy catchupProxy = new CatchupProxy();
		
		catchup = getCatchup(catchupProxy);
		
		if (catchup==null)
			return false;
		else return true;
	}
	
	public void receive(Object msg) {
		
	}
	
	abstract void process(Object msg);
	
	/**
	 * This should atomically get the Catchup object for the feed
	 * and register the argument, proxy, as a recipient for incoming
	 * messages for the feed.
	 * 
	 * @param proxy
	 * @return
	 */
	abstract Catchup getCatchup(AbstractFeedProxy proxy);
	
	@Override
	public void endCatchup() {
		// TODO Auto-generated method stub
	}
	
}
