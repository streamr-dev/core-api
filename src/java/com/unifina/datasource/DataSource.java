package com.unifina.datasource;

import com.unifina.data.FeedEvent;
import com.unifina.data.IFeedRequirement;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeed;
import com.unifina.service.FeedService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.SignalPath;
import com.unifina.utils.Globals;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * DataSource is a class global to the current run context. It handles
 * the creation of data feeds either explicitly (via DataSource#connectFeed(feed)) 
 * or implicitly according to a SignalPath's needs (via DataSource#connectSignalPath(signalPath)).
 *
 * @author Henri
 */
public abstract class DataSource {
	private static final Logger log = Logger.getLogger(DataSource.class);

	private final Set<SignalPath> signalPaths = new HashSet<>();
	private final List<IStartListener> startListeners = new ArrayList<>();
	private final List<IStopListener> stopListeners = new ArrayList<>();
	private final Map<Long, AbstractFeed> feedById = new HashMap<>();
	private final Globals globals;
	private final boolean isHistoricalFeed;
	private boolean started = false;


	public DataSource(boolean isHistoricalFeed, Globals globals) {
		this.isHistoricalFeed = isHistoricalFeed;
		this.globals = globals;
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
			register(it);
		}
	}

	/**
	 * Registers an object with this DataSource.
	 */
	public void register(Object o) {
		if (o instanceof IStreamRequirement) {
			subscribeToFeed(o, ((IStreamRequirement) o).getStream().getFeed());
		} else if (o instanceof IFeedRequirement) {
			subscribeToFeed(o, ((IFeedRequirement) o).getFeed());
		}

		if (o instanceof ITimeListener) {
			getEventQueue().addTimeListener((ITimeListener) o);
		}
		if (o instanceof IDayListener) {
			getEventQueue().addDayListener((IDayListener) o);
		}
	}

	/**
	 * Adds an IStartListener to this DataSource. Its onStart() method will be called just before
	 * starting the data flow. If the DataSource is already started, the listener will be called
	 * immediately.
	 */
	public void addStartListener(IStartListener startListener) {
		startListeners.add(startListener);
		if (started) {
			startListener.onStart();
		}
	}

	public void addStopListener(IStopListener stopListener) {
		stopListeners.add(stopListener);
	}

	public void startFeed() {
		for (IStartListener startListener : startListeners) {
			startListener.onStart();
		}

		try {
			started = true;
			doStartFeed();
		} catch (Exception e) {
			log.error("Exception thrown while running feed", e);
			throw new RuntimeException("Error while running feed", e);
		}
	}

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

	/**
	 * Enqueue an event into event queue
	 */
	public void enqueueEvent(FeedEvent feedEvent) {
		getEventQueue().enqueue(feedEvent);
	}

	public AbstractFeed getFeedById(Long id) {
		return feedById.get(id);
	}

	protected abstract DataSourceEventQueue getEventQueue();

	protected abstract void onSubscribedToFeed(AbstractFeed feed);

	protected abstract void doStartFeed() throws Exception;

	protected abstract void doStopFeed() throws Exception;

	protected Collection<AbstractFeed> getFeeds() {
		return feedById.values();
	}

	protected Iterable<SignalPath> getSerializableSignalPaths() {
		List<SignalPath> serializableSps = new ArrayList<>();
		for (SignalPath sp : signalPaths) {
			if (sp.isSerializable()) {
				serializableSps.add(sp);
			}
		}
		return serializableSps;
	}

	private void subscribeToFeed(Object subscriber, Feed feedDomain) {
		AbstractFeed feed = createFeed(feedDomain);
		try {
			log.debug("subscribeToFeed: subscriber " + subscriber + " subscribing to feed " + feedDomain.getName());
			feed.subscribe(subscriber);
		} catch (Exception e) {
			throw new RuntimeException("Failed to subscribe " + subscriber + " to feed " + feed, e);
		}
		onSubscribedToFeed(feed);
	}

	private AbstractFeed createFeed(Feed domain) {
		// Feed implementation already instantiated?
		AbstractFeed feed = feedById.get(domain.getId());
		if (feed == null) {
			log.debug("createFeed: Instantiating new feed described by domain object "+domain+(isHistoricalFeed ? " in historical mode" : " in realtime mode"));

			FeedService feedService = Holders.getApplicationContext().getBean(FeedService.class);
			feed = feedService.instantiateFeed(domain, isHistoricalFeed, globals);
			feed.setEventQueue(getEventQueue());
			feedById.put(domain.getId(), feed);
		} else {
			log.debug("createFeed: Feed " + feed + " exists, using that instance.");
		}

		return feed;
	}
}
