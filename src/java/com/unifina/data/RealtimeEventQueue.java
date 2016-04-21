package com.unifina.data;

import java.util.ArrayDeque;
import java.util.Queue;

import com.codahale.metrics.*;
import com.unifina.service.MetricsService;
import grails.util.Holders;
import org.apache.log4j.Logger;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.utils.Globals;

public class RealtimeEventQueue extends DataSourceEventQueue {

	private long elapsedTime;
	private int eventCounter;

	private final MetricsService metricsService = (MetricsService) Holders.getGrailsApplication().getMainContext().getBean("metricsService");
	private final Meter eventsProcessed = metricsService.getMeter("eventsProcessed.realtime");

	boolean firstEvent = true;

	/**
	 * Write log every loggingInterval events; set to 0 for no logging
	 */
	private static final int loggingInterval = 10000;
	private static final Logger log = Logger.getLogger(RealtimeEventQueue.class);

	public RealtimeEventQueue(Globals globals, DataSource dataSource) {
		super(globals, dataSource);
	}


	@Override
	protected Queue<FeedEvent> initQueue() {
		return new ArrayDeque<>();
	}

	protected void doStart() throws Exception {

		log.info("The realtime event queue is starting!");
		sync = true;

		while (!abort) {
			FeedEvent event;
			synchronized (queue) {
				while (queue.isEmpty() && !abort) {
					try {
						queue.wait();
					} catch (InterruptedException ignored) {

					}
				}

				if (abort) {
					log.info("doStart: aborting");
					return;
				}

				event = queue.poll();
			}

			long startTime = System.nanoTime();
			process(event);

			if (loggingInterval > 0) {
				elapsedTime += System.nanoTime() - startTime;
				eventCounter++;
				eventsProcessed.mark();
				if (eventCounter >= loggingInterval) {
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
				// Notify timelisteners
				reportTime(time);
				// Update global time
				globals.time = event.timestamp;
			}

			if (event.recipient != null)
				event.recipient.receive(event);

			return true;

		} catch (Exception e) {
			// Catch any Exception to prevent crashing the whole thing
			log.error("Exception while processing event!", e);
			return true;
		}
	}

}
