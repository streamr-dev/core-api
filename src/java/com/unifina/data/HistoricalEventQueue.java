package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HistoricalEventQueue extends DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(HistoricalEventQueue.class);

	private final int speed;
	private final long simTimeStart;
	private long realTimeStart;
	private long eventCounter = 0;
	private long timeSpentProcessing = 0;

	public HistoricalEventQueue(Globals globals, DataSource dataSource) {
		this(globals, dataSource, DEFAULT_CAPACITY);
	}

	public HistoricalEventQueue(Globals globals, DataSource dataSource, int capacity) {
		super(globals, dataSource, capacity);
		speed = readSpeedConfiguration(globals);
		simTimeStart = globals.getStartDate().getTime() - (globals.getStartDate().getTime() % 1000);

		/**
		 * Queue events at lower and upper bounds of selected playback range to ensure that MasterClock
		 * ticks through the range even in the absence of feed data.
		 */
		queue.add(PlaybackMessage.newStartEvent(globals.getStartDate()));
		queue.add(PlaybackMessage.newEndEvent(globals.getEndDate()));
	}

	private boolean shouldKeepProcessing() {
		return !isAborted() && (!queue.isEmpty() || !asyncExecutor.getQueue().isEmpty());
	}

	@Override
	public void runEventLoopUntilDone() {
		// Set start time.
		globals.time = new Date(simTimeStart);
		initTimeReporting(simTimeStart);
		realTimeStart = System.currentTimeMillis();

		while (shouldKeepProcessing()) {
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

			// Create time events as needed to fill in the "gaps" between actual events.
			// This is needed if there are no actual stream events covering every second.
			while (event.getTimestamp().getTime() > lastReportedClockTick + CLOCK_TICK_INTERVAL_MILLIS && !isAborted()) {
				// Handle time events until the next real event in the queue happens on a reported second
				Date next = new Date(lastReportedClockTick + CLOCK_TICK_INTERVAL_MILLIS);
				final Event<ClockTick> clockTickEvent = new Event<>(new ClockTick(next), next, 0L, null);
				waitIfNecessary(clockTickEvent);
				handleEvent(clockTickEvent);
			}

			waitIfNecessary(event);
			handleEvent(event);
		}

		// Report statistics
		long feedElapsedTime = System.currentTimeMillis() - realTimeStart;
		log.debug("PERFORMANCE: Processed "+ eventCounter +" events.");
		if (eventCounter > 0) {
			log.debug("PERFORMANCE: Processing took "+((timeSpentProcessing/ eventCounter)/1000.0)+" microseconds per event.");
			log.debug("PERFORMANCE: Entire processing took "+feedElapsedTime+" milliseconds or "+((feedElapsedTime*1000)/ eventCounter)+" microseconds per event.");
		}
	}

	private void waitIfNecessary(Event event) {
		final long time = event.getTimestamp().getTime();

		// Check if a delay is needed
		if (speed != 0) {
			long realTimeElapsed = System.currentTimeMillis() - realTimeStart;
			long simTimeMax = realTimeElapsed*speed + simTimeStart;
			long diff = time - simTimeMax;
			if (diff > 0) {
				try {
					Thread.sleep((int)(diff/speed));
				} catch (InterruptedException e) { /* ignore */ }
			}
		}
	}

	private void handleEvent(Event event) {
		final long time = event.getTimestamp().getTime();
		final long startTime = System.nanoTime();

		// Events across different streams/producers aren't necessarily ordered in time.
		// Never report out-of-order time to modules to prevent weird effects.
		// Instead, always use the latest observed time as globals.time.
		if (globals.time==null || time > globals.time.getTime()) {
			tickClockIfNecessary(time);

			// Update global time
			globals.time = event.getTimestamp();
		}

		// Handle event
		event.dispatch();

		eventQueueMetrics.countEvent(System.nanoTime() - startTime, 0);
		timeSpentProcessing += System.nanoTime() - startTime;
		eventCounter++;
	}

	private static int readSpeedConfiguration(Globals globals) {
		Map ctx = globals.getSignalPathContext();
		return ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0;
	}

}
