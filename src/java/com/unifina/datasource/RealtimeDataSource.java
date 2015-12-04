package com.unifina.datasource;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.unifina.serialization.SerializationRequest;
import com.unifina.utils.MapTraversal;
import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.data.IFeed;
import com.unifina.data.RealtimeEventQueue;
import com.unifina.feed.ICatchupFeed;
import com.unifina.utils.Globals;

public class RealtimeDataSource extends DataSource {

	Timer secTimer = new Timer();
	
	private PriorityQueue<FeedEvent> catchupQueue = new PriorityQueue<>();
	
//	private long catchupQueueTicket = 0;
	private boolean abort = false;
	
	private static final Logger log = Logger.getLogger(RealtimeDataSource.class);
	
	public RealtimeDataSource(Globals globals) {
		super(false, globals);
	}

	@Override
	protected DataSourceEventQueue initEventQueue() {
		return new RealtimeEventQueue(globals, this);
	}
	
	@Override
	protected void doStartFeed() throws Exception {
		// Check catchup
		List<ICatchupFeed> catchupFeeds = new ArrayList<>();
		
		for (IFeed it : feedByClass.values()) {
			if (it instanceof ICatchupFeed && ((ICatchupFeed)it).startCatchup())
				catchupFeeds.add((ICatchupFeed)it);
		}
		
		if (catchupFeeds.size()>0)
			log.info("Processing catchup messages from "+catchupFeeds.size()+" feeds..");

		// While catching up, any events added to the realtime eventQueue must be added to the catchupQueue instead!
		Queue<FeedEvent> originalQueue = eventQueue.queue;
		eventQueue.queue = catchupQueue;

		processCatchups(catchupFeeds);
		
		// Let all catchup feeds know that catchup has ended,
		// even if their startCatchup() returned false and thus
		// they are not in catchupFeeds
		for (IFeed it : feedByClass.values()) {
			if (it instanceof ICatchupFeed)
				((ICatchupFeed)it).endCatchup();
		}
		
		eventQueue.queue = originalQueue;
		
		if (catchupFeeds.size()>0)
			log.info("Catchup complete.");
		
		if (!abort) {
			for (IFeed it : feedByClass.values())
				it.startFeed();

			final Date now = new Date();
			secTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							if (eventQueue.isEmpty()) {
								FeedEvent timeEvent = new FeedEvent();
								timeEvent.timestamp = new Date();
								eventQueue.enqueue(timeEvent);
							}
						}
					},
					new Date(now.getTime() + (1000 - (now.getTime()%1000))), // Time till next even second
					1000);   // Repeat every second

			// Serialization
			secTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					eventQueue.enqueue(SerializationRequest.makeFeedEvent(getSignalPath()));

				}
			}, new Date(now.getTime() + (1000 - (now.getTime()%1000))), // Time till next even second
					globals.serializationIntervalInMillis());


			// This will block indefinitely until the feed is stopped!
			eventQueue.start();
		
		}
		
		log.info("RealtimeDataSource has stopped.");
	}

	private void processCatchups(List<ICatchupFeed> feeds) {
		
		// Insert first event from each feed into the queue
		for (ICatchupFeed feed : feeds) {
			FeedEvent[] events = feed.getNextEvents();
			if (events!=null) {
				for (FeedEvent it : events) {
//					it.queueTicket = catchupQueueTicket++
//					catchupQueue.add(it)
					eventQueue.enqueue(it);
				}
			}
		}
		
		while (!eventQueue.isEmpty() && !abort) {
			FeedEvent event = eventQueue.poll();
			eventQueue.process(event);
			
			if (event.feed instanceof ICatchupFeed) {
				FeedEvent[] events = ((ICatchupFeed)event.feed).getNextEvents();
				if (events!=null) {
					for (FeedEvent it : events) {
						eventQueue.enqueue(it);
//						it.queueTicket = catchupQueueTicket++
//						catchupQueue.add(it) 
					}
				}
				else {
//					log.debug("Catchup feed "+event.feed+" depleted.");
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
		
		for (IFeed it : feedByClass.values()) {
			try {
				it.stopFeed();
			} catch (Exception e) {
				log.error(e);
			}
		}
		eventQueue.abort();
	}

	
}
