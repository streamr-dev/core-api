package com.unifina.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.unifina.data.IFeed;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.service.FeedService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.utils.Globals;
import com.unifina.signalpath.SignalPath;

/**
 * DataSource is a class global to the current run context. It handles
 * the creation of data feeds either explicitly (via DataSource#connectFeed(feed)) 
 * or implicitly according to a SignalPath's needs (via DataSource#connectSignalPath(signalPath)).
 * 
 * @author Henri
 */
public abstract class DataSource {
	
//	public static long eventStartNanos;
	
	protected DataSourceEventQueue eventQueue;
	
	HashMap<String,IFeed> feedByClass = new HashMap<>();
//	protected List<ITimeListener> timeListeners = []
//	protected List<IDayListener> dateListeners = []
	protected List<IStartListener> startListeners = new ArrayList<>();
	protected List<IStopListener> stopListeners = new ArrayList<>();
	protected boolean isHistoricalFeed = true;

	protected Globals globals;
	
	public DataSource(boolean isHistoricalFeed, Globals globals) {
		this.isHistoricalFeed = isHistoricalFeed;
		this.globals = globals;
		eventQueue = initEventQueue();
	}
	
	protected abstract DataSourceEventQueue initEventQueue();
	
	private static final Logger log = Logger.getLogger(DataSource.class);
	
	public DataSourceEventQueue getEventQueue() {
		return eventQueue;
	}
	
	/**
	 * Checks if the DataSource understands what to do with the object.
	 * If true is returned, a subsequent call to register(object) must succeed.
	 * @param object
	 * @return
	 */
	public boolean canRegister(Object o) {
		return (o instanceof IStreamRequirement || o instanceof ITimeListener || o instanceof IDayListener);
	}
	
	/**
	 * Registers an object with this DataSource. Must succeed if canRegister has returned true.
	 * @param object
	 */
	public void register(Object o) {
		boolean registered = false;
		if (o instanceof IStreamRequirement) {
			registerModule((IStreamRequirement)o);
			registered = true;
		}
		if (o instanceof ITimeListener) {
			eventQueue.addTimeListener((ITimeListener) o);
			registered = true;
		}
		if (o instanceof IDayListener) {
			eventQueue.addDayListener((IDayListener) o);
			registered = true;
		}
		
//		if (!registered) 
//			throw new IllegalArgumentException("I don't know what to do with "+o+"!");
	}
	
	protected IFeed registerModule(IStreamRequirement o) {
		IFeed feed = createFeed(o.getStream().getFeed());
		try {
			feed.subscribe(o);
			
//			// TODO: should this wiring be implemented more generally?
//			IEventRecipient rcpt = feed.getEventRecipient(o);
//			if (rcpt!=null) {
//				register(rcpt);
//				if (rcpt instanceof AbstractEventRecipient) {
//					((AbstractEventRecipient) rcpt).register(o);
//				}
//			}
		} catch (Exception e) {
			log.error("Failed to subscribe "+o+" to feed "+feed,e);
			throw new RuntimeException("Failed to subscribe "+o+" to feed "+feed,e);
		}
		return feed;
	}
	
	public void addStartListener(IStartListener startListener) {
		startListeners.add(startListener);
	}
	
	public void addStopListener(IStopListener stopListener) {
		stopListeners.add(stopListener);
	}
	
	public IFeed createFeed(Feed domain) {
		FeedService feedService = (FeedService) globals.getGrailsApplication().getMainContext().getBean("feedService");
		if (feedService == null)
			feedService = new FeedService();
		
		// Create the feed required by this OrderBook
		String feedClass = feedService.getFeedClass(domain, isHistoricalFeed);
		
		// Feed already created?
		IFeed feed = feedByClass.get(feedClass);
		
		// Should we instantiate a new feed?
		if (feed==null) {
			feed = feedService.instantiateFeed(domain, isHistoricalFeed, globals);
			feed.setEventQueue(getEventQueue());
			feedByClass.put(feedClass, feed);
		}
		
		return feed;
	}
	
	public Collection<IFeed> getFeeds() {
		return feedByClass.values();
	}
	
	public void startFeed() {
		for (int i=0;i<startListeners.size();i++)
			startListeners.get(i).onStart();
		// Possible ConcurrentModificationException
//		for (IStartListener it : startListeners)
//			it.onStart(); 

		try {
			doStartFeed();
		} catch (Exception e) {
			log.error("Exception thrown while running feed",e);
			throw new RuntimeException("Error while running feed",e);
		}
	}
	
	protected abstract void doStartFeed() throws Exception;
	
	public void stopFeed() {
		try {
			doStopFeed();
		} catch (Exception e) {
			log.error("Exception thrown while stopping feed",e);
			throw new RuntimeException("Error while stopping feed",e);
		}
		
		for (IStopListener it : stopListeners)
			it.onStop();
	}
	
	protected abstract void doStopFeed() throws Exception;
	
	/**
	 * Connects a SignalPath to this DataSource. This means connecting to
	 * all the feeds required by the Modules in the SignalPath and registering
	 * the Modules with the associated feeds.
	 * @param sp
	 */
	public void connectSignalPath(SignalPath sp) {
//		signalPaths << sp
		for (AbstractSignalPathModule it : sp.getModules()) {
			if (canRegister(it))
				register(it);
		}
	}
	
}
