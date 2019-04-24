package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.utils.DateRange;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class HistoricalEventQueue extends DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(HistoricalEventQueue.class);

	private final int speed;
	private EventQueueMetrics eventQueueMetrics = new HistoricalEventQueueMetrics();

	public HistoricalEventQueue(Globals globals, DataSource dataSource) {
		super(false, globals, dataSource);
		speed = readSpeedConfiguration(globals);
	}

	@Override
	protected Queue<Event> createQueue(int capacity) {
		Queue<Event> queue = new PriorityQueue<>(capacity);

		/**
		 * Queue events at lower and upper bounds of selected playback range to ensure that MasterClock ticks through
		 * range even in the absence of feed data.
		 */
		queue.add(PlaybackMessage.newStartEvent(globals.getStartDate()));
		queue.add(PlaybackMessage.newEndEvent(globals.getEndDate()));

		return queue;
	}

	@Override
	public void doStart() throws Exception {
		long feedStartTime = System.currentTimeMillis();
		long eventCounter = 0;
		long timeSpentProcessing = 0;

		// Set start time
		globals.time = peek().getTimestamp();

		long time = globals.time.getTime();

		DateRange range = null;
		if (globals.getSignalPathContext().containsKey("timeOfDayFilter")) {
			String start = ((Map)globals.getSignalPathContext().get("timeOfDayFilter")).get("timeOfDayStart").toString();
			String end = ((Map)globals.getSignalPathContext().get("timeOfDayFilter")).get("timeOfDayEnd").toString();
			range = new DateRange(start, end);
			range.setBaseDate(globals.time);
		}
		long todBegin = (range != null ? range.getBeginTime() : 0);
		long todEnd = (range != null ? range.getEndTime() : 0);

		initTimeReporting(time - (time%1000));

		long simTimeStart = (range==null ? time : Math.max(time,range.getBeginTime()));
		long realTimeStart = System.currentTimeMillis();

		while (!isEmpty() && !isAborted()) {
			// Insert time events to the queue if necessary to "tick" the clock every second.
			// This is needed if there are no actual stream events covering every second.
			if (peek().getTimestamp().getTime() > lastHandledTime + 1000) {
				// Insert a time event to the front of the queue, before the next real event
				Date next = new Date(lastHandledTime + 1000);
				final Event<ClockTick> event = new Event<>(new ClockTick(next), next, 0L, null);
				enqueue(event);
			}

			Event event = poll();
			time = event.getTimestamp().getTime();

			// Check if a delay is needed
			if (speed != 0 && (range == null || time > todBegin && time < todEnd)) {
				long realTimeElapsed = System.currentTimeMillis() - realTimeStart;
				long simTimeMax = realTimeElapsed*speed + simTimeStart;
				long diff = time - simTimeMax;
				if (diff > 0) {
					try {
						Thread.sleep((int)(diff/speed));
					} catch (InterruptedException e) {
						if (isAborted()) {
							return;
						}
					}
				}
			}

			long startTime = System.nanoTime();
			boolean processed = process(event);
			eventQueueMetrics.countEvent(System.nanoTime() - startTime, 0);
			timeSpentProcessing += System.nanoTime() - startTime;
			eventCounter++;

			// If not processed, an event which precedes this event must have been added to the queue. Add the event back to queue.
			if (!processed) {
				enqueue(event);
			}
		}

		// Report statistics
		long feedElapsedTime = System.currentTimeMillis() - feedStartTime;
		log.debug("PERFORMANCE: Processed "+ eventCounter +" events.");
		if (eventCounter >0) {
			log.debug("PERFORMANCE: Processing took "+((timeSpentProcessing/ eventCounter)/1000.0)+" microseconds per event.");
			log.debug("PERFORMANCE: Entire processing took "+feedElapsedTime+" milliseconds or "+((feedElapsedTime*1000)/ eventCounter)+" microseconds per event.");
		}
	}

	@Override
	public boolean process(Event event) {
		long time = event.getTimestamp().getTime();

		// Never go backwards in time
		// TODO: this shouldn't be a concern...
		if (globals.time==null || time > globals.time.getTime()) {
			reportTime(time);

			// TimeListeners can post events into the queue, make sure that this event is still the most recent one
			if (!isEmpty() && event.compareTo(peek()) >= 0) {
				return false;
			}

			// Update global time
			globals.time = event.getTimestamp();
		}

		// Handle event
		event.dispatch();
		return true;
	}

	@Override
	protected EventQueueMetrics retrieveMetricsAndReset() {
		EventQueueMetrics returnMetrics = eventQueueMetrics;
		eventQueueMetrics = new HistoricalEventQueueMetrics();
		return returnMetrics;
	}

	@Override
	protected void doStop() {
		// Don't need to do anything
	}

	private static int readSpeedConfiguration(Globals globals) {
		Map ctx = globals.getSignalPathContext();
		return ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0;
	}
}
