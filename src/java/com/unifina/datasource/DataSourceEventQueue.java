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
import java.util.concurrent.*;

public abstract class DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(DataSourceEventQueue.class);

	protected static final int DEFAULT_CAPACITY = 10000;
	protected static final int CLOCK_TICK_INTERVAL_MILLIS = 1000; // tick every second

	private final MasterClock masterClock;
	private final List<IDayListener> dayListeners = new ArrayList<>();
	protected BlockingQueue<Event> queue;
	protected final Globals globals;
	private boolean abort = false;
	protected long lastReportedClockTick = 0;
	private DateTime nextDay;
	protected Thread consumingThread;

	protected ThreadPoolExecutor asyncExecutor;

	protected EventQueueMetrics eventQueueMetrics = new EventQueueMetrics();

	public DataSourceEventQueue(Globals globals, DataSource dataSource) {
		this(globals, dataSource, DEFAULT_CAPACITY);
	}

	public DataSourceEventQueue(Globals globals, DataSource dataSource, int capacity) {
		this.globals = globals;
		masterClock = new MasterClock(globals, dataSource);
		queue = createQueue(capacity);
	}

	/**
	 * The default implementation is an ArrayBlockingQueue.
	 * @param capacity The hard capacity limit of the queue. After reaching this size the queue should block on offer.
	 * @return
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
		abort = false;
		asyncExecutor = new ThreadPoolExecutor(1, 1,
			0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>());

		consumingThread = Thread.currentThread();

		try {
			runEventLoopUntilDone();
		} finally {
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
		if (Thread.currentThread() == consumingThread) {
			enqueueAsync(event);
		} else {
			enqueueSync(event);
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
	 * This is useful for internal event queuing, i.e. cases where
	 * we want to eventually put something in the event queue even if
	 * it's congested with incoming data.
	 */
	private void enqueueAsync(Event event) {
		asyncExecutor.submit(() -> {
			enqueueSync(event);
		});
	}

	/**
	 * Aborts the queue processing at earliest opportunity, and causes the
	 * call to runEventLoopUntilDone() to exit.
	 */
	public void abort() {
		abort = true;
	}

	/**
	 * Should run the main event loop. The function only returns when
	 * there is nothing left to do (processing is aborted or finished).
	 * This should be called from the Thread that is used for event
	 * processing.
	 */
	protected abstract void runEventLoopUntilDone();

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

}
