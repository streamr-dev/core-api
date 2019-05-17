package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.exceptions.StreamFieldChangedException;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class RealtimeEventQueue extends DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(RealtimeEventQueue.class);
	private static final int LOGGING_INTERVAL = 10000; // set to 0 for no logging

	private boolean firstEvent = true;

	public RealtimeEventQueue(Globals globals, DataSource dataSource) {
		this(globals, dataSource, DEFAULT_CAPACITY);
	}

	public RealtimeEventQueue(Globals globals, DataSource dataSource, int capacity) {
		super(globals, dataSource, capacity);
	}

	@Override
	protected void runEventLoopUntilDone() {
		log.info("runEventLoopUntilDone: starting!");

		while (!isAborted()) {
			Event event = null;
			try {
				event = queue.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error(e);
			}

			if (event == null) {
				// Timed out while waiting for new events. Just keep trying until aborted.
				continue;
			}

			long startTime = System.nanoTime();
			long startTimeInMillis = System.currentTimeMillis();

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
			} catch (StreamFieldChangedException e) {
				throw e;
			} catch (Exception e) {
				// Catch any Exception to prevent crashing the whole thing
				log.error("Exception while processing event: "+event.toString(), e);
			}

			long now = System.nanoTime();

			eventQueueMetrics.countEvent(now - startTime, startTimeInMillis - event.getTimestamp().getTime());

			// Log some metrics
			int eventCounter = 0;
			long elapsedTime = 0;

			if (LOGGING_INTERVAL > 0) {
				elapsedTime += now - startTime;
				eventCounter++;

				if (eventCounter >= LOGGING_INTERVAL) {
					double perEvent = (elapsedTime / (double) eventCounter) / 1000.0;
					log.info("Processed " + eventCounter + " events in " + elapsedTime + " nanoseconds. " +
			 				 "That's " + perEvent + " microseconds per event.");
					eventCounter = 0;
					elapsedTime = 0;
				}
			}

		}

		log.info("runEventLoopUntilDone: aborted.");
	}

}
