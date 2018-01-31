package com.unifina.datasource;

import com.unifina.data.FeedEvent;
import com.unifina.feed.MasterClock;
import com.unifina.utils.Globals;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

public abstract class DataSourceEventQueue {
	private final MasterClock masterClock;
	private final List<IDayListener> dayListeners = new ArrayList<>();
	private final Object syncLock;
	private Queue<FeedEvent> queue;
	protected final Globals globals;
	private boolean abort = false;
	private long lastHandledTime = 0;
	private DateTime nextDay;
	private long queueTicket = 0;

	/**
	 * Must be set to true if events are enqueued from multiple threads
	 */

	public DataSourceEventQueue(boolean sync, Globals globals, DataSource dataSource) {
		this.syncLock = sync ? new Object() : null;
		this.globals = globals;
		masterClock = new MasterClock(globals, dataSource);
		queue = initQueue();
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
		doStart();
	}

	public void enqueue(FeedEvent event) {
		if (syncLock != null) {
			synchronized (syncLock) {
				event.queueTicket = queueTicket++;
				queue.add(event);
				syncLock.notify(); // Notify the SignalPathRunner thread
			}
		} else {
			event.queueTicket = queueTicket++;
			queue.add(event);
		}
	}

	public void abort() {
		abort = true;
		if (syncLock != null) {
			synchronized (syncLock) {
				syncLock.notify(); // Notify the SignalPathRunner thread
			}
			doStop();
		}
	}

	protected abstract Queue<FeedEvent> initQueue();

	protected abstract void doStart() throws Exception;

	/**
	 * @return True if the event was processed, false if it was not (then it should be returned to the queue).
	 */
	public abstract boolean process(FeedEvent event);

	protected abstract void doStop();

	protected boolean isAborted() {
		return abort;
	}

	protected void initTimeReporting(long firstTime) {
		if (lastHandledTime == 0) {
			lastHandledTime = firstTime;
			DateTime now = new DateTime(firstTime, DateTimeZone.UTC);
			nextDay = now.minusMillis(now.getMillisOfDay()).plusDays(1);
		}
	}

	protected void reportTime(long eventTime) {
		/*
		 * With event-based clock in historical mode, the time between events can be multiple seconds.
		 * However each second should be reported. New events may appear in the queue between
		 * reporting each second, we must check for this!
		 */
		int initialQueueSize = queue.size();

		while (lastHandledTime + 1000 <= eventTime && queue.size() == initialQueueSize) {
			lastHandledTime += 1000;
			Date d = new Date(lastHandledTime);
			globals.time = d;

			// Handle possible day turn
			if (lastHandledTime > nextDay.getMillis()) {
				int dlCount = dayListeners.size();

				// Report the new day
				for (int i = 0; i < dlCount; i++) {
					dayListeners.get(i).onDay(d);
				}

				nextDay = nextDay.plusDays(1);
			}

			masterClock.receive(new FeedEvent<>(null, d, null));
		}
	}

	protected Object getSyncLock() {
		return syncLock;
	}

	protected boolean isEmpty() {
		return queue.isEmpty();
	}

	protected Queue<FeedEvent> getQueue() {
		return queue;
	}

	protected void setQueue(Queue<FeedEvent> queue) {
		this.queue = queue;
	}

	protected void addWithoutUpdatingTicket(FeedEvent feedEvent) {
		queue.add(feedEvent);
	}

	protected FeedEvent peek() {
		return queue.peek();
	}

	protected FeedEvent poll() {
		return queue.poll();
	}
}
