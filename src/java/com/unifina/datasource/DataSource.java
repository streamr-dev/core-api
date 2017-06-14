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

	protected DataSourceEventQueue eventQueue;
	
	private HashMap<Long, AbstractFeed> feedById = new HashMap<>();
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
	private boolean started = false;
	
	public DataSourceEventQueue getEventQueue() {
		return eventQueue;
	}
	
	/**
	 * Checks if the DataSource understands what to do with the object.
	 * If true is returned, a subsequent call to register(object) must succeed.
	 * @param o
	 * @return
	 */
	public boolean canRegister(Object o) {
		return (o instanceof IFeedRequirement || o instanceof IStreamRequirement || o instanceof ITimeListener || o instanceof IDayListener);
	}
	
	/**
	 * Registers an object with this DataSource. Must succeed if canRegister has returned true.
	 * @param o
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

	/**
	 * Adds an IStartListener to this DataSource. Its onStart() method will be called just before
	 * starting the data flow. If the DataSource is already started, the listener will be called
	 * immediately.
     */
	public void addStartListener(IStartListener startListener) {
		startListeners.add(startListener);
		if (isStarted()) {
			startListener.onStart();
		}
	}
	
	public void addStopListener(IStopListener stopListener) {
		stopListeners.add(stopListener);
	}
	
	public AbstractFeed createFeed(Feed domain) {
		FeedService feedService = (FeedService) globals.getGrailsApplication().getMainContext().getBean("feedService");
		if (feedService == null)
			feedService = new FeedService();

		// Feed implementation already instantiated?
		AbstractFeed feed = feedById.get(domain.getId());
		if (feed==null) {
			// Create the instance
			log.debug("createFeed: Instantiating new feed described by domain object "+domain+(isHistoricalFeed ? " in historical mode" : " in realtime mode"));
			feed = feedService.instantiateFeed(domain, isHistoricalFeed, globals);
			feed.setEventQueue(getEventQueue());
			feedById.put(domain.getId(), feed);
		}
		else {
			log.debug("createFeed: Feed "+feed+" exists, using that instance.");
		}
		
		return feed;
	}

	public AbstractFeed getFeedById(Long id) {
		return feedById.get(id);
	}

	public Collection<AbstractFeed> getFeeds() {
		return feedById.values();
	}
	
	public void startFeed() {
		for (int i=0;i<startListeners.size();i++)
			startListeners.get(i).onStart();

		try {
			started = true;
			doStartFeed();
		} catch (Exception e) {
			log.error("Exception thrown while running feed",e);
			throw new RuntimeException("Error while running feed",e);
		}
	}
	
	protected abstract void doStartFeed() throws Exception;
	
	public void stopFeed() {
		// re-throw if exception happened, but only after all listeners have had chance to clean up
		Exception stopException = null;
		try {
			doStopFeed();
		} catch (Exception e) {
			log.error("Exception thrown while stopping feed", e);
			stopException = e;
		}
		for (IStopListener it : stopListeners) {
			try {
				it.onStop();
			} catch (Exception e) {
				log.error("Exception thrown while stopping feed", e);
				stopException = e;
			}
		}

		if (stopException != null) {
			throw new RuntimeException("Error while stopping feed", stopException);
		}
	}
	
	protected abstract void doStopFeed() throws Exception;

	public boolean isStarted() {
		return started;
	}

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
