package com.unifina.datasource;

import java.util.*;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventQueue;
import com.unifina.feed.MasterClock;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.utils.Globals;

public abstract class DataSourceEventQueue implements IEventQueue {

	protected Queue<FeedEvent> queue;
	protected Globals globals;
	protected boolean abort = false;

	private MasterClock masterClock;
	//	private ArrayList<ITimeListener> timeListeners = new ArrayList<>();
	private ArrayList<IDayListener> dayListeners = new ArrayList<>();

	private long lastReportedSec = 0;
	private long lastReportedDayNumber = 0;

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

		while (lastReportedSec + 1000 <= time && queue.size() == initialQueueSize) {
			lastReportedSec += 1000;
			Date d = new Date(lastReportedSec);
			globals.time = d;

			// Is this a new day?
			Calendar c = new GregorianCalendar();
			c.setTime(d);
			int dayNumber = c.get(GregorianCalendar.DAY_OF_WEEK);
			if (lastReportedDayNumber != dayNumber) {
				dlCount = dayListeners.size();

				// TODO: remove this hack. The point is that all modules must be cleared before calling onDay(d)
				for (i = 0; i < dlCount; i++) {
					if (dayListeners.get(i) instanceof AbstractSignalPathModule) {
						((AbstractSignalPathModule) dayListeners.get(i)).clear();
					}
				}

				// Report the new day
				for (i = 0; i < dlCount; i++) {
					dayListeners.get(i).onDay(d);
				}

				lastReportedDayNumber = dayNumber;
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
