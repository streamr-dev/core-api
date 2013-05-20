package com.unifina.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue;
import com.unifina.utils.Globals;
import com.unifina.utils.TimeOfDayUtil;

public class BacktestEventQueue extends DataSourceEventQueue {
	
	public BacktestEventQueue(Globals globals, DataSource dataSource) {
		super(globals, dataSource);
	}

	private ArrayList<IFeed> feeds = new ArrayList<>();
	private HashMap<String,IFeed> feedsByName = new HashMap<>();

	long feedStartTime = 0;
	int speed = 0;

//	long timeSpentPolling = 0;
//	long timeSpentQueuing = 0;
	
	@Override
	protected Queue<FeedEvent> initQueue() {
		// Not thread-safe! It should not matter because currently everything runs in a single thread.
		return new /*MinCachePriorityQueue<>(); //*/PriorityQueue<>();
	}
	
	public IFeed getFeed(String feedClass) {
		return feedsByName.get(feedClass);
	}
	
	public void addFeed(IFeed feed) {
		if (!feeds.contains(feed)) {
			feeds.add(feed);
			feedsByName.put(feed.getClass().getCanonicalName(), feed);
		}
//		else throw new IllegalStateException("The event loop already contains this feed!");
	}
		
	public void abort() {
		super.abort();
		for (IFeed feed : feeds)
			try {
				feed.stopFeed();
			} catch (Exception e) {}
	}
	
	public void doStart() throws Exception {
		feedStartTime = System.currentTimeMillis();
		
		// Insert first event from each feed into the queue
		for (IFeed feed : feeds) {
			feed.startFeed();
			if (feed instanceof IBacktestFeed) {
				FeedEvent event = ((IBacktestFeed)feed).getNext();
				if (event!=null)
					enqueue(event);
			}
		}
		
		if (queue.isEmpty())
			return;
		
		// Init global time if it has not already been initialized
		if (globals.time == null) {
			Date firstDate;
			
			// Take the beginDate from signalPathContext if defined
			if (globals.getSignalPathContext()!=null && globals.getSignalPathContext().containsKey("beginDate")) {
				firstDate = (Date) globals.getSignalPathContext().get("beginDate");
			}
			// Otherwise get it from the first event
			else firstDate = queue.peek().timestamp;
			
			globals.time = firstDate;
		}
		
		/**
		 * Initialize some values
		 */
		long time = globals.time.getTime();
		
		TimeOfDayUtil todUtil = null;
		if (globals.getSignalPathContext()!=null && globals.getSignalPathContext().containsKey("timeOfDayFilter")) {
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
		
		long realTimeElapsed = System.currentTimeMillis() - realTimeStart;
		long simTimeMax = realTimeElapsed*speed;
		
		while (!queue.isEmpty() && !abort) {
//			long startTime = System.nanoTime();
			FeedEvent event = queue.poll();
//			timeSpentPolling = System.nanoTime() - startTime;
			
			time = event.timestamp.getTime();
			
			// Check if a delay is needed
			if (speed != 0 && time > todBegin && time < todEnd) {
				realTimeElapsed = System.currentTimeMillis() - realTimeStart;
				simTimeMax = realTimeElapsed*speed + simTimeStart;
//				println "minTime: "+df.format(new Date(minTime))+", simTimeMax: "+df.format(new Date(simTimeMax))
				long diff = time - simTimeMax;
				if (diff>0) {
//					println "Sleeping for "+diff/speed
					try {
						Thread.sleep((int)(diff/speed));
					} catch (InterruptedException e) {
						if (abort) {
							return;
						}
					}
				}
			}
			
			// Start timer
			long startTime = System.nanoTime();
			
			boolean processed = process(event);
			
			timeSpentProcessing += System.nanoTime() - startTime;
			eventCounter++;
			
			// If not processed, add to queue without updating the ticket (don't call enqueue(event))
//			startTime = System.nanoTime();
			if(!processed)
				queue.add(event);
			else if (event.feed instanceof IBacktestFeed) {
				FeedEvent next = ((IBacktestFeed)event.feed).getNext();
				if (next!=null)
					enqueue(next);
			}
//			timeSpentQueuing += System.nanoTime() - startTime;
			
		}
		
		long feedElapsedTime = System.currentTimeMillis() - feedStartTime;
		
		System.out.println("PERFORMANCE: Processed "+eventCounter+" events.");
		System.out.println("PERFORMANCE: Processing took "+((timeSpentProcessing/eventCounter)/1000.0)+" microseconds per event.");
//		System.out.println("PERFORMANCE: Polling the event queue took "+((timeSpentPolling/eventCounter)/1000.0)+" microseconds per event.");
//		System.out.println("PERFORMANCE: Enqueuing took "+((timeSpentQueuing/eventCounter)/1000.0)+" microseconds per event.");
		System.out.println("PERFORMANCE: Entire processing took "+feedElapsedTime+" milliseconds or "+((feedElapsedTime*1000)/eventCounter)+" microseconds per event.");
		
	}

	@Override
	public boolean process(FeedEvent event) {
		
		long time = event.timestamp.getTime();
		
		// Never go backwards in time
		if (globals.time==null || time > globals.time.getTime()) {
			// Notify timelisteners
			reportTime(time);
			
			// TimeListeners can post events into the queue, we must make sure that this event is still the most recent one
			if (!queue.isEmpty() && event.compareTo(queue.peek()) >= 0)
				return false;
			
			// Update global time
			globals.time = event.timestamp;
			
		}

		// Handle event
//		DataSource.eventStartNanos = System.nanoTime();
		event.recipient.receive(event);
		return true;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}


}
