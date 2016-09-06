package com.unifina.datasource;

import java.util.*;

import com.unifina.domain.signalpath.Canvas;
import org.apache.log4j.Logger;

import com.unifina.data.IFeedRequirement;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeed;
import com.unifina.service.FeedService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.SignalPath;
import com.unifina.utils.Globals;

/**
 * DataSource is a class global to the current run context. It handles
 * the creation of data feeds either explicitly (via DataSource#connectFeed(feed)) 
 * or implicitly according to a SignalPath's needs (via DataSource#connectSignalPath(signalPath)).
 * 
 * @author Henri
 */
public abstract class DataSource {

	private Set<SignalPath> signalPaths = new HashSet<>();
	
//	public static long eventStartNanos;
	
	protected DataSourceEventQueue eventQueue;
	
	HashMap<String,AbstractFeed> feedByClass = new HashMap<>();
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
		return (o instanceof IFeedRequirement || o instanceof IStreamRequirement || o instanceof ITimeListener || o instanceof IDayListener);
	}
	
	/**
	 * Registers an object with this DataSource. Must succeed if canRegister has returned true.
	 * @param object
	 */
	public void register(Object o) {
		if (o instanceof IStreamRequirement) {
			subscribeToFeed(o, ((IStreamRequirement) o).getStream().getFeed());
		}
		else if (o instanceof IFeedRequirement) {
			subscribeToFeed(o, ((IFeedRequirement) o).getFeed());
		}
		
		if (o instanceof ITimeListener) {
			eventQueue.addTimeListener((ITimeListener) o);
		}
		if (o instanceof IDayListener) {
			eventQueue.addDayListener((IDayListener) o);
		}

	}

	protected AbstractFeed subscribeToFeed(Object subscriber, Feed feedDomain) {
		AbstractFeed feed = createFeed(feedDomain);
		try {
			log.debug("subscribeToFeed: subscriber "+subscriber+" subscribing to feed "+feedDomain.getName());
			feed.subscribe(subscriber);
		} catch (Exception e) {
			log.error("Failed to subscribe "+subscriber+" to feed "+feed,e);
			throw new RuntimeException("Failed to subscribe "+subscriber+" to feed "+feed,e);
		}
		return feed;
	}
	
	public void addStartListener(IStartListener startListener) {
		startListeners.add(startListener);
	}
	
	public void addStopListener(IStopListener stopListener) {
		stopListeners.add(stopListener);
	}
	
	public AbstractFeed createFeed(Feed domain) {
		FeedService feedService = (FeedService) globals.getGrailsApplication().getMainContext().getBean("feedService");
		if (feedService == null)
			feedService = new FeedService();
		
		// Create the feed implementation
		String feedClass = feedService.getFeedClass(domain, isHistoricalFeed);
		
		// Feed already created?
		AbstractFeed feed = feedByClass.get(feedClass);
		
		// Should we instantiate a new feed?
		if (feed==null) {
			log.debug("createFeed: Instatiating new feed "+feedClass);
			feed = feedService.instantiateFeed(domain, isHistoricalFeed, globals);
			feed.setEventQueue(getEventQueue());
			feedByClass.put(feedClass, feed);
		}
		else {
			log.debug("createFeed: Feed "+feedClass+" exists, using that instance.");
		}
		
		return feed;
	}
	
	public Collection<AbstractFeed> getFeeds() {
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

		for (IStopListener it : stopListeners) {
			it.onStop();
		}
	}
	
	protected abstract void doStopFeed() throws Exception;
	
	/**
	 * Connects a SignalPath to this DataSource. This means connecting to
	 * all the feeds required by the Modules in the SignalPath and registering
	 * the Modules with the associated feeds.
	 * @param sp
	 */
	public void connectSignalPath(SignalPath sp) {
		signalPaths.add(sp);
		for (AbstractSignalPathModule it : sp.getModules()) {
			if (canRegister(it))
				register(it);
		}
	}

	protected Iterable<SignalPath> getSerializableSignalPaths() {
		List<SignalPath> serializableSps = new ArrayList<>();
		for (SignalPath sp : signalPaths) {
			Canvas canvas = sp.getCanvas();
			if (canvas != null && !canvas.getAdhoc()) {
				serializableSps.add(sp);
			}
		}
		return serializableSps;
	}
}
