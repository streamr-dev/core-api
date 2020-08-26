package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.utils.BoundedPriorityBlockingQueue;
import com.unifina.utils.Globals;
import com.unifina.utils.ThreadUtil;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * DataSourceEventQueue for historical playbacks. Main difference to DataSourceEventQueue:
 *
 * - Uses a PriorityQueue as the underlying queue. This is to make sure all events are
 *   processed in proper order.
 * - Can throttle playback speed based on speed setting
 * - Minor differences in time reporting
 */
public class HistoricalEventQueue extends DataSourceEventQueue {

	private final int speed;
	private final long simTimeStart;
	private long realTimeStart;

	public HistoricalEventQueue(Globals globals, DataSource dataSource) {
		this(globals, dataSource, DEFAULT_CAPACITY);
	}

	public HistoricalEventQueue(Globals globals, DataSource dataSource, int capacity) {
		super(globals, dataSource, capacity, false);
		speed = readSpeedConfiguration(globals);
		simTimeStart = globals.getTime().getTime();
	}

	/**
	 * @return A PriorityQueue instead of a normal queue
	 */
	@Override
	protected BlockingQueue<Event> createQueue(int capacity) {
		return new BoundedPriorityBlockingQueue<>(capacity);
	}

	@Override
	protected void beforeStart() {
		// Remember wall-clock time to control playback speed
		realTimeStart = System.currentTimeMillis();
	}

	@Override
	protected void beforeEvent(Event event) {
		// Create time events as needed to fill in the "gaps" between actual events.
		// This is needed if there are no actual stream events covering every second.
		while (event.getTimestamp().getTime() > getLastReportedClockTick() + CLOCK_TICK_INTERVAL_MILLIS && !isAborted()) {
			// Handle time events until the next real event in the queue happens on a reported second
			Date next = new Date(getLastReportedClockTick() + CLOCK_TICK_INTERVAL_MILLIS);
			final Event<ClockTick> clockTickEvent = new Event<>(new ClockTick(next), next, 0L, null);
			sleepIfNecessary(clockTickEvent);
			super.handleEvent(clockTickEvent);
		}

		// Sleep if we're not running at full speed and it's time to sleep
		if (!isAborted()) {
			sleepIfNecessary(event);
		}
	}

	private void sleepIfNecessary(Event event) {
		final long time = event.getTimestamp().getTime();

		// Check if a delay is needed
		if (speed != 0) {
			long realTimeElapsed = System.currentTimeMillis() - realTimeStart;
			long simTimeMax = realTimeElapsed*speed + simTimeStart;
			long diff = time - simTimeMax;
			if (diff > 0) {
				ThreadUtil.sleep((int)(diff/speed));
			}
		}
	}

	private static int readSpeedConfiguration(Globals globals) {
		Map ctx = globals.getSignalPathContext();
		return ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0;
	}

}
