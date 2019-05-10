package com.unifina.datasource;

import com.unifina.data.ClockTick;
import com.unifina.data.Event;
import com.unifina.data.EventQueueMetrics;
import com.unifina.feed.MasterClock;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(DataSourceEventQueue.class);

	private static final int QUEUE_HARD_LIMIT = 10000;
	protected static final int CLOCK_TICK_INTERVAL_MILLIS = 1000; // tick every second

	private final MasterClock masterClock;
	private final List<IDayListener> dayListeners = new ArrayList<>();
	private BlockingQueue<Event> queue;
	protected final Globals globals;
	private boolean abort = false;
	protected long lastReportedClockTick = 0;
	private DateTime nextDay;

	protected EventQueueMetrics eventQueueMetrics = new EventQueueMetrics();

	public DataSourceEventQueue(Globals globals, DataSource dataSource) {
		this.globals = globals;
		masterClock = new MasterClock(globals, dataSource);
		queue = createQueue(QUEUE_HARD_LIMIT);
	}

	public void addTimeListener(ITimeListener timeListener) {
		masterClock.register(timeListener);
	}

	public void addDayListener(IDayListener dayListener) {
		if (!dayListeners.contains(dayListener)) {
			dayListeners.add(dayListener);
		}
	}

	/**
	 * The call to this method should block until the queue is aborted or all events have been processed.
	 */
	public void start() throws Exception {
		abort = false;
		runEventLoopUntilAborted();
	}

	public void enqueue(Event event) {
		try {
			boolean success = queue.offer(event, 30, TimeUnit.SECONDS);
			if (!success) {
				log.error("enqueue: Timed out while trying to enqueue event " + event);
			}
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	public void abort() {
		abort = true;
	}

	/**
	 * Should return a concurrent blocking queue with given max capacity.
	 */
	protected abstract BlockingQueue<Event> createQueue(int capacity);

	protected abstract void runEventLoopUntilAborted() throws Exception;

	/**
	 * @return True if the event was dispatched, false if it was not (then it should be returned to the queue).
	 */
	public abstract boolean dispatch(Event event);

	EventQueueMetrics retrieveMetricsAndReset() {
		EventQueueMetrics returnMetrics = eventQueueMetrics;
		eventQueueMetrics = new EventQueueMetrics();
		return returnMetrics;
	}

	protected boolean isAborted() {
		return abort;
	}

	protected void initTimeReporting(long firstTime) {
		if (lastReportedClockTick == 0) {
			lastReportedClockTick = firstTime;
			DateTime now = new DateTime(firstTime, DateTimeZone.UTC);
			nextDay = now.minusMillis(now.getMillisOfDay()).plusDays(1);
		}
	}

	protected void tickClockIfNecessary(long eventTime) {
		if (lastReportedClockTick + CLOCK_TICK_INTERVAL_MILLIS <= eventTime) {
			lastReportedClockTick += CLOCK_TICK_INTERVAL_MILLIS;
			Date d = new Date(lastReportedClockTick);
			globals.time = d;

			// Handle possible day turn
			if (lastReportedClockTick > nextDay.getMillis()) {
				int dlCount = dayListeners.size();

				// Report the new day
				for (int i = 0; i < dlCount; i++) {
					dayListeners.get(i).onDay(d);
				}

				nextDay = nextDay.plusDays(1);
			}

			final ClockTick tick = new ClockTick(d);
			masterClock.accept(tick);
		}
	}

	protected boolean isEmpty() {
		return queue.isEmpty();
	}

	protected BlockingQueue<Event> getQueue() {
		return queue;
	}

	protected Event peek() {
		return queue.peek();
	}

	protected Event poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
		return queue.poll(timeout, timeUnit);
	}

}
