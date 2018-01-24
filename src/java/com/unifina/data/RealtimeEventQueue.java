package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.signalpath.StopRequest;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

public class RealtimeEventQueue extends DataSourceEventQueue implements IEventRecipient {
	private static final Logger log = Logger.getLogger(RealtimeEventQueue.class);
	private static final int LOGGING_INTERVAL = 10000; // set to 0 for no logging

	private boolean firstEvent = true;

	public RealtimeEventQueue(Globals globals, DataSource dataSource) {
		super(true, globals, dataSource);
	}

	@Override
	protected Queue<FeedEvent> initQueue() {
		return new ArrayDeque<>();
	}

	protected void doStart() throws Exception {
		log.info("The realtime event queue is starting!");

		int eventCounter = 0;
		long elapsedTime = 0;

		while (!isAborted()) {
			FeedEvent event = waitForAndPullFeedEvent();
			if (event == null) {
				log.info("doStart: aborting");
				return;
			}

			long startTime = System.nanoTime();
			process(event);

			// Report processing
			if (LOGGING_INTERVAL > 0) {
				elapsedTime += System.nanoTime() - startTime;
				eventCounter++;
				if (eventCounter >= LOGGING_INTERVAL) {
					double perEvent = (elapsedTime / eventCounter) / 1000.0;
					log.info("Processed " + eventCounter + " events in " + elapsedTime + " nanoseconds. " +
			 				 "That's " + perEvent + " microseconds per event.");
					eventCounter = 0;
					elapsedTime = 0;
				}
			}

		}
	}

	@Override
	public boolean process(FeedEvent event) {

		long time = event.timestamp.getTime();

		if (firstEvent) {
			firstEvent = false;
			initTimeReporting((time - (time % 1000)) - 1000);
		}

		try {
			// Never go backwards in time
			if (globals.time == null || time > globals.time.getTime()) {
				reportTime(time);
				globals.time = event.timestamp;
			}

			event.deliver();

			return true;

		} catch (Exception e) {
			// Catch any Exception to prevent crashing the whole thing
			log.error("Exception while processing event: "+event.toString(), e);
			return true;
		}
	}

	@Override
	protected void doStop() {}

	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof StopRequest) {
			log.info("Received abort request, aborting...");
			abort();
		} else {
			log.warn("Unrecognized request " + event);
		}
	}

	private FeedEvent waitForAndPullFeedEvent() {
		synchronized (getSyncLock()) {
			while (isEmpty() && !isAborted()) {
				try {
					getSyncLock().wait();
				} catch (InterruptedException ignored) {}
			}
			return isAborted() ? null : poll();
		}
	}
}
