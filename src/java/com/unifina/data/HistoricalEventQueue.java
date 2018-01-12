package com.unifina.data;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.feed.AbstractFeed;
import com.unifina.feed.AbstractHistoricalFeed;
import com.unifina.utils.Globals;
import com.unifina.utils.TimeOfDayUtil;
import org.apache.log4j.Logger;

import java.util.*;

public class HistoricalEventQueue extends DataSourceEventQueue {
	private static final Logger log = Logger.getLogger(HistoricalEventQueue.class);

	private final List<AbstractFeed> feeds = new ArrayList<>();
	private final int speed;

	public HistoricalEventQueue(Globals globals, DataSource dataSource) {
		super(false, globals, dataSource);
		speed = readSpeedConfiguration(globals);
	}

	public void addFeed(AbstractFeed feed) {
		if (!feeds.contains(feed)) {
			feeds.add(feed);
		}
	}

	@Override
	protected Queue<FeedEvent> initQueue() {
		// Not thread-safe! It should not matter because currently everything runs in a single thread.
		return new PriorityQueue<>();
	}

	@Override
	public void doStart() throws Exception {
		long feedStartTime = System.currentTimeMillis();
		long eventCounter = 0;
		long timeSpentProcessing = 0;

		// Insert first event from each feed into the queue
		for (AbstractFeed feed : feeds) {
			feed.startFeed();
			if (feed instanceof AbstractHistoricalFeed && ((AbstractHistoricalFeed)feed).hasNext()) {
				FeedEvent event = ((AbstractHistoricalFeed)feed).next();
				enqueue(event);
			}
		}

		// TODO: this shouldn't be unclear at this point, either we set it here or somewhere else, but not both.
		// Init global time if it has not already been initialized
		if (globals.time == null) {
			// Take the beginDate from signalPathContext if defined
			if (globals.getSignalPathContext().containsKey("beginDate")) {
				globals.time = (Date) globals.getSignalPathContext().get("beginDate");
			} else {
				// Otherwise get it from the first event
				globals.time = peek().timestamp;
			}
		}

		/**
		 * Queue events at lower and upper bounds of selected playback range to ensure that MasterClock ticks through
		 * range even in the absence of feed data.
		 */
		addWithoutUpdatingTicket(PlaybackMessage.newStartEvent(globals.getStartDate()));
		addWithoutUpdatingTicket(PlaybackMessage.newEndEvent(globals.getEndDate()));

		/**
		 * Initialize some values
		 */
		long time = globals.time.getTime();

		TimeOfDayUtil todUtil = null;
		if (globals.getSignalPathContext().containsKey("timeOfDayFilter")) {
			String start = ((Map)globals.getSignalPathContext().get("timeOfDayFilter")).get("timeOfDayStart").toString();
			String end = ((Map)globals.getSignalPathContext().get("timeOfDayFilter")).get("timeOfDayEnd").toString();
			todUtil = new TimeOfDayUtil(start, end, globals.getUserTimeZone());
			todUtil.setBaseDate(globals.time);
		}
		long todBegin = (todUtil != null ? todUtil.getBeginTime() : 0);
		long todEnd = (todUtil != null ? todUtil.getEndTime() : 0);

		// Subtract one second so the first reported time will be the first second
		initTimeReporting(time - (time%1000) - 1000);

		long simTimeStart = (todUtil==null ? time : Math.max(time,todUtil.getBeginTime()));
		long realTimeStart = System.currentTimeMillis();

		while (!isEmpty() && !isAborted()) {
			FeedEvent event = poll();
			time = event.timestamp.getTime();

			// Check if a delay is needed
			if (speed != 0 && time > todBegin && time < todEnd) {
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
			timeSpentProcessing += System.nanoTime() - startTime;
			eventCounter++;

			// If not processed, add to queue without updating the ticket (don't call enqueue(event))
			if (!processed) {
				addWithoutUpdatingTicket(event);
			} else if (event.feed instanceof AbstractHistoricalFeed && ((AbstractHistoricalFeed) event.feed).hasNext()) {
				FeedEvent next = ((AbstractHistoricalFeed) event.feed).next();
				enqueue(next);
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
	public boolean process(FeedEvent event) {
		long time = event.timestamp.getTime();

		// Never go backwards in time
		// TODO: this shouldn't be a concern...
		if (globals.time==null || time > globals.time.getTime()) {
			reportTime(time);

			// TimeListeners can post events into the queue, make sure that this event is still the most recent one
			if (!isEmpty() && event.compareTo(peek()) >= 0) {
				return false;
			}

			// Update global time
			globals.time = event.timestamp;
		}

		// Handle event
		event.deliver();
		return true;
	}

	@Override
	protected void doStop() {
		for (AbstractFeed feed : feeds) {
			try {
				feed.stopFeed();
			} catch (Exception e) {
				log.error("Feed error while stopping HistoricalEventQueue: " + e.getMessage());
			}
		}
	}

	private static int readSpeedConfiguration(Globals globals) {
		Map ctx = globals.getSignalPathContext();
		return ctx.containsKey("speed") ? Integer.parseInt(ctx.get("speed").toString()) : 0;
	}
}
