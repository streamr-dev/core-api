package com.unifina.datasource;

import java.util.*;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventQueue;
import com.unifina.feed.MasterClock;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StopRequest;
import com.unifina.utils.Globals;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public abstract class DataSourceEventQueue implements IEventQueue {

	protected Queue<FeedEvent> queue;
	protected Globals globals;
	protected boolean abort = false;

	private MasterClock masterClock;
	//	private ArrayList<ITimeListener> timeListeners = new ArrayList<>();
	private ArrayList<IDayListener> dayListeners = new ArrayList<>();

	private long lastReportedSec = 0;
	private DateTime nextDay;

	protected long timeSpentProcessing = 0;
	protected long eventCounter = 0;

	protected long queueTicket = 0;

	private int dlCount;
	//	private int tlCount;
	private int i;

	/**
	 * Must be set to true if events are enqueued from multiple threads
	 */
	protected boolean sync = false;

	public DataSourceEventQueue(Globals globals, DataSource dataSource) {
		this.globals = globals;
		masterClock = new MasterClock(globals, dataSource);
		queue = initQueue();
	}

	protected abstract Queue<FeedEvent> initQueue();

	@Override
	public void addTimeListener(ITimeListener timeListener) {
		masterClock.register(timeListener);
//		if (!timeListeners.contains(timeListener))
//			timeListeners.add(timeListener);
	}

	@Override
	public void addDayListener(IDayListener dayListener) {
		if (!dayListeners.contains(dayListener)) {
			dayListeners.add(dayListener);
		}
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * The call to this method should block until the queue is aborted or all events have been processed.
	 */
	@Override
	public void start() throws Exception {
		abort = false;
		doStart();
	}

	protected abstract void doStart() throws Exception;

	protected void initTimeReporting(long firstTime) {
		if (lastReportedSec == 0) {
			lastReportedSec = firstTime;
		}
	}

	protected void reportTime(long time) {
		/**
		 * With event-based clock in backtest, the time between events can be multiple seconds.
		 * However each second should be reported. New events may appear in the queue between
		 * reporting each second, we must check for this!
		 */
		int initialQueueSize = queue.size();

		if (nextDay == null) {
			DateTime now = new DateTime(lastReportedSec, DateTimeZone.UTC);
			nextDay = now.minusMillis(now.getMillisOfDay()).plusDays(1);
		}

		while (lastReportedSec + 1000 <= time && queue.size() == initialQueueSize) {
			lastReportedSec += 1000;
			Date d = new Date(lastReportedSec);
			globals.time = d;

			if (lastReportedSec > nextDay.getMillis()) {
				dlCount = dayListeners.size();

				// Report the new day
				for (i = 0; i < dlCount; i++) {
					dayListeners.get(i).onDay(d);
				}

				nextDay = nextDay.plusDays(1);
			}

			FeedEvent timeEvent = new FeedEvent();
			timeEvent.timestamp = d;
			masterClock.receive(timeEvent);
		}
	}

	@Override
	public void enqueue(FeedEvent event) {
		if (sync) {
			synchronized (queue) {
				event.queueTicket = queueTicket++;
				queue.add(event);
				queue.notify(); // Notify the SignalPathRunner thread
			}
		} else {
			event.queueTicket = queueTicket++;
			queue.add(event);
		}
	}

	/**
	 * @param event
	 * @return True if the event was processed, false if it was not (then it should be returned to the queue).
	 */
	public abstract boolean process(FeedEvent event);

	@Override
	public void abort() {
		abort = true;
		synchronized (queue) {
			queue.notify(); // Notify the SignalPathRunner thread
		}
	}

	public Queue<FeedEvent> getQueue() {
		return queue;
	}

	public void setQueue(Queue<FeedEvent> queue) {
		this.queue = queue;
	}

	public FeedEvent peek() {
		return queue.peek();
	}

	public FeedEvent poll() {
		return queue.poll();
	}
}
