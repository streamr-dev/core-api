package com.unifina.datasource;

import com.unifina.data.ClockTick;
import com.unifina.data.Event;
import com.unifina.data.EventQueueMetrics;
import com.unifina.exceptions.StreamFieldChangedException;
import com.unifina.feed.TimePropagationRoot;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(DataSourceEventQueue.class);

	protected static final int DEFAULT_CAPACITY = 10000;
	private static final int ASYNC_QUEUE_CAPACITY = 10000;
	protected static final int CLOCK_TICK_INTERVAL_MILLIS = 1000; // tick every second

	private final TimePropagationRoot masterClock;
	private final List<IDayListener> dayListeners = new ArrayList<>();
	protected BlockingQueue<Event> queue;
	protected final Globals globals;
	private boolean aborted = false;
	private final boolean measureLatency;
	private long lastReportedClockTick = Long.MIN_VALUE;
	private DateTime nextDay;
	private Thread consumingThread;
	private ThreadPoolExecutor asyncExecutor;
	private EventQueueMetrics eventQueueMetrics = new EventQueueMetrics();

	public DataSourceEventQueue(Globals globals, DataSource dataSource) {
		this(globals, dataSource, DEFAULT_CAPACITY, true);
	}

	public DataSourceEventQueue(Globals globals, DataSource dataSource, int capacity, boolean measureLatency) {
		this.globals = globals;
		this.masterClock = new TimePropagationRoot(dataSource);
		this.queue = createQueue(capacity);
		this.measureLatency = measureLatency;
	}

	/**
	 * The default implementation is an ArrayBlockingQueue.
	 * @param capacity The hard capacity limit of the queue. After reaching this size the queue should block on offer.
	 */
	protected BlockingQueue<Event> createQueue(int capacity) {
		return new ArrayBlockingQueue<>(capacity);
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
	 * The call to this method blocks until the queue is aborted or all events have been processed.
	 */
	public void start() throws Exception {
		aborted = false;

		// Single threaded executor to maintain the order of events
		asyncExecutor = new ThreadPoolExecutor(1, 1,
			0L, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<>(ASYNC_QUEUE_CAPACITY),
			// Reject handled, called when the queue is at capacity
			(r, executor) -> {
				log.error("Async executor queue is full, and an event was dropped!");
			});

		consumingThread = Thread.currentThread();

		try {
			beforeStart();
			log.info("Starting event loop!");

			while (!isAborted()) {
				Event event = pollUntilTimeout();

				if (event == null) {
					// Timed out while waiting for new events. Just keep trying until aborted.
					continue;
				}

				beforeEvent(event);
				handleEvent(event);
			}
		} finally {
			aborted = true;
			log.info("Event loop stopped.");

			asyncExecutor.shutdownNow();

			// Report statistics
			log.debug(eventQueueMetrics.toString());
		}
	}

	/**
	 * Enqueues the event into this event queue.
	 *
	 * If the method is called from the same Thread which is consuming
	 * messages from the queue, the event will be async-queued as a failsafe
	 * to avoid deadlocking the thread in case the queue is full.
	 */
	public void enqueue(Event event) {
		if (!aborted) {
			if (Thread.currentThread() == consumingThread) {
				enqueueAsync(event);
			} else {
				enqueueSync(event);
			}
		}
	}

	/**
	 * Enqueues an event synchronously. Blocks if the queue is full, until the
	 * event fits into the queue. Throws if no space becomes available before a timeout.
	 */
	private void enqueueSync(Event event) {
		try {
			boolean success = queue.offer(event, 30, TimeUnit.SECONDS);
			if (!success) {
				throw new RuntimeException("Timed out while trying to enqueue event " + event);
			}
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	/**
	 * Non-blocking method to asynchronously queue events.
	 * This should only be called when the calling Thread is the same
	 * as the consuming Thread (this happens when the processing of an
	 * event produces more events into the queue) to prevent a deadlock.
	 */
	private void enqueueAsync(final Event event) {
		asyncExecutor.submit(() -> enqueueSync(event));
	}

	/**
	 * Blocks up to 1 second, waiting for an event to be available in
	 * the queue. Returns null if there wasn't one.
	 */
	private Event pollUntilTimeout() {
		try {
			return queue.poll(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * Gets called immediately before the event loop starts.
	 */
	protected void beforeStart() {}

	/**
	 * Gets called immediately before the event is handled.
	 */
	protected void beforeEvent(Event event) {}


	/**
	 * Aborts the queue processing as soon as possible, and causes the
	 * call to start() to exit. Any events remaining in the queue,
	 * or on their way to the queue, will be discarded.
	 */
	public void abort() {
		aborted = true;
	}

	EventQueueMetrics retrieveMetricsAndReset() {
		EventQueueMetrics returnMetrics = eventQueueMetrics;
		eventQueueMetrics = new EventQueueMetrics();
		return returnMetrics;
	}

	protected boolean isAborted() {
		return aborted;
	}

	private boolean isTimeReportingInitialized() {
		return lastReportedClockTick > Long.MIN_VALUE;
	}

	protected void initTimeReporting(long firstTime) {
		if (!isTimeReportingInitialized()) {
			lastReportedClockTick = firstTime;
			globals.time = new Date(firstTime);

			DateTime now = new DateTime(firstTime, DateTimeZone.UTC);
			nextDay = now.minusMillis(now.getMillisOfDay()).plusDays(1);
		}
	}

	private void tickClockIfNecessary(long eventTime) {
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

	/**
	 * Ticks the clock, dispatches the event, and updates the eventQueueMetrics.
	 */
	protected final void handleEvent(Event event) {

		// Start timers for metrics calculation
		final long eventTime = event.getTimestamp().getTime();
		final long startTimeMillis = System.currentTimeMillis();
		final long startTimeNanos = System.nanoTime();

		try {
			if (!isTimeReportingInitialized()) {
				long time = event.getTimestamp().getTime();
				initTimeReporting((time - (time % 1000)) - 1000);
			}

			// Events across different streams/producers aren't necessarily ordered in time.
			// Never report out-of-order time to modules to prevent weird effects.
			// Instead, always use the latest observed time as globals.time.
			if (eventTime > globals.time.getTime()) {
				tickClockIfNecessary(eventTime);

				// Update global time
				globals.time = event.getTimestamp();
			}

			// Handle event
			event.dispatch();
		} catch (StreamFieldChangedException e) {
			log.error("StreamFieldChangedException thrown, stopping the queue by escalating the error!");
			throw e;
		} catch (Exception e) {
			// Catch any Exception to prevent crashing the whole thing
			log.error("Exception while processing event: "+event.toString(), e);
		}

		eventQueueMetrics.countEvent(System.nanoTime() - startTimeNanos, measureLatency ? startTimeMillis - eventTime : 0);
	}

	protected BlockingQueue<Event> getQueue() {
		return queue;
	}

	public int size() {
		return queue.size();
	}

	public boolean isFull() {
		return queue.remainingCapacity() == 0;
	}

	public int remainingCapacity() {
		return queue.remainingCapacity();
	}

	protected long getLastReportedClockTick() {
		return lastReportedClockTick;
	}

}
