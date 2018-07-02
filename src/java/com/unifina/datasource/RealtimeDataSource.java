package com.unifina.datasource;


import com.unifina.data.FeedEvent;
import com.unifina.data.RealtimeEventQueue;
import com.unifina.feed.AbstractFeed;
import com.unifina.serialization.SerializationRequest;
import com.unifina.service.SerializationService;
import com.unifina.signalpath.SignalPath;
import com.unifina.signalpath.StopRequest;
import com.unifina.utils.Globals;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RealtimeDataSource extends DataSource {
	private static final Logger log = Logger.getLogger(RealtimeDataSource.class);

	private final Timer secTimer = new Timer();
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
