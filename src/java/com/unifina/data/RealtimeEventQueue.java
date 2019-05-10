package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.exceptions.StreamFieldChangedException;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RealtimeEventQueue extends DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(RealtimeEventQueue.class);
	private static final int LOGGING_INTERVAL = 10000; // set to 0 for no logging

	private boolean firstEvent = true;

	public RealtimeEventQueue(Globals globals, DataSource dataSource) {
		super(globals, dataSource);
	}

	@Override
	protected BlockingQueue<Event> createQueue(int capacity) {
		return new ArrayBlockingQueue<>(capacity);
	}

	protected void runEventLoopUntilAborted() {
		log.info("The realtime event queue is starting!");

		int eventCounter = 0;
		long elapsedTime = 0;

		while (!isAborted()) {
			Event event = null;
			try {
				event = poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error(e);
			}

			if (event == null) {
				// Timed out while waiting for new events. Just keep trying until aborted.
				continue;
			}

			long startTime = System.nanoTime();
			long startTimeInMillis = System.currentTimeMillis();
			dispatch(event);
			long now = System.nanoTime();

			eventQueueMetrics.countEvent(now - startTime, startTimeInMillis - event.getTimestamp().getTime());

			// Report processing
			if (LOGGING_INTERVAL > 0) {
				elapsedTime += now - startTime;
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

		log.info("runEventLoopUntilAborted: aborted.");
	}

	@Override
	public boolean dispatch(Event event) {
		long time = event.getTimestamp().getTime();

		if (firstEvent) {
			firstEvent = false;
			initTimeReporting((time - (time % 1000)) - 1000);
		}

		try {
			// Never go backwards in time
			if (globals.time == null || time > globals.time.getTime()) {
				tickClockIfNecessary(time);
				globals.time = event.getTimestamp();
			}

			event.dispatch();

			return true;
		} catch (StreamFieldChangedException e) {
			throw e;
		} catch (Exception e) {
			// Catch any Exception to prevent crashing the whole thing
			log.error("Exception while processing event: "+event.toString(), e);
			return true;
		}
	}

}
