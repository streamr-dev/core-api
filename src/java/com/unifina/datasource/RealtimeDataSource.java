package com.unifina.datasource;


import com.unifina.data.FeedEvent;
import com.unifina.data.RealtimeEventQueue;
import com.unifina.feed.AbstractFeed;
import com.unifina.feed.ICatchupFeed;
import com.unifina.serialization.SerializationRequest;
import com.unifina.service.SerializationService;
import com.unifina.signalpath.SignalPath;
import com.unifina.signalpath.StopRequest;
import com.unifina.utils.Globals;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.util.*;

public class RealtimeDataSource extends DataSource {
	private static final Logger log = Logger.getLogger(RealtimeDataSource.class);

	private final Timer secTimer = new Timer();
	private final PriorityQueue<FeedEvent> catchupQueue = new PriorityQueue<>();
	private final RealtimeEventQueue eventQueue;
	private boolean abort = false;

	public RealtimeDataSource(Globals globals) {
		super(false, globals);
		eventQueue = new RealtimeEventQueue(globals, this);
	}

	@Override
	protected DataSourceEventQueue getEventQueue() {
		return eventQueue;
	}

	@Override
	protected void onSubscribedToFeed(AbstractFeed feed) {}

	@Override
	protected void doStartFeed() throws Exception {
		// Check catchup
		List<ICatchupFeed> catchupFeeds = new ArrayList<>();

		for (AbstractFeed it : getFeeds()) {
			if (it instanceof ICatchupFeed && ((ICatchupFeed) it).startCatchup()) {
				catchupFeeds.add((ICatchupFeed) it);
			}
		}

		if (catchupFeeds.size() > 0) {
			log.info("Processing catchup messages from " + catchupFeeds.size() + " feeds..");
		}

		// While catching up, any events added to the realtime eventQueue must be added to the catchupQueue instead!
		Queue<FeedEvent> originalQueue = eventQueue.getQueue();
		eventQueue.setQueue(catchupQueue);

		processCatchups(catchupFeeds);

		// Let all catchup feeds know that catchup has ended,
		// even if their startCatchup() returned false and thus
		// they are not in catchupFeeds
		for (AbstractFeed it : getFeeds()) {
			if (it instanceof ICatchupFeed) {
				((ICatchupFeed) it).endCatchup();
			}
		}

		eventQueue.setQueue(originalQueue);

		if (catchupFeeds.size() > 0) {
			log.info("Catchup complete.");
		}

		if (!abort) {
			for (AbstractFeed it : getFeeds()) {
				it.startFeed();
			}

			final Date now = new Date();
			secTimer.scheduleAtFixedRate(new TimerTask() {
				 	@Override
				 	public void run() {
					 	if (eventQueue.isEmpty()) {
							eventQueue.enqueue(new FeedEvent<>(null, new Date(), null));
					 	}
					}
			 	},
				new Date(now.getTime() + (1000 - (now.getTime() % 1000))), // Time till next even second
				1000);   // Repeat every second

			// Serialization
			SerializationService serializationService = Holders.getApplicationContext().getBean(SerializationService.class);
			long serializationIntervalInMs = serializationService.serializationIntervalInMillis();

			if (serializationIntervalInMs > 0) {
				for (final SignalPath signalPath : getSerializableSignalPaths()) {
					secTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							eventQueue.enqueue(SerializationRequest.makeFeedEvent(signalPath));
						}
					}, serializationIntervalInMs, serializationIntervalInMs);
				}
			}

			// This will block indefinitely until the feed is stopped!
			eventQueue.start();
		}

		log.info("RealtimeDataSource has stopped.");
	}

	private void processCatchups(List<ICatchupFeed> feeds) {

		// Insert first event from each feed into the queue
		for (ICatchupFeed feed : feeds) {
			FeedEvent[] events = feed.getNextEvents();
			if (events != null) {
				for (FeedEvent it : events) {
					eventQueue.enqueue(it);
				}
			}
		}

		while (!eventQueue.isEmpty() && !abort) {
			FeedEvent event = eventQueue.poll();
			eventQueue.process(event);

			if (event.feed instanceof ICatchupFeed) {
				FeedEvent[] events = ((ICatchupFeed) event.feed).getNextEvents();
				if (events != null) {
					for (FeedEvent it : events) {
						eventQueue.enqueue(it);
					}
				}
			}
		}
	}

	@Override
	protected void doStopFeed() throws Exception {
		log.info("Stopping RealtimeDataSource...");
		abort = true;
		secTimer.cancel();
		secTimer.purge();

		for (AbstractFeed it : getFeeds()) {
			try {
				it.stopFeed();
			} catch (Exception e) {
				log.error(e);
			}
		}

		// Final serialization requests
		for (SignalPath signalPath : getSerializableSignalPaths()) {
			eventQueue.enqueue(SerializationRequest.makeFeedEvent(signalPath));
		}

		// Stop request
		Date date = new Date();
		eventQueue.enqueue(new FeedEvent<>(new StopRequest(date), date, eventQueue));
	}


}
