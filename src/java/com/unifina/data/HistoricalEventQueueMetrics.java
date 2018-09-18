package com.unifina.data;

public class HistoricalEventQueueMetrics implements EventQueueMetrics {
	private long elapsedTime = 0;
	private long numOfEvents = 0;

	@Override
	public void countEvent(long timeDiff, long delay) {
		elapsedTime += timeDiff;
		numOfEvents++;
	}

	public static double aggregateEventsPerSecond(Iterable<EventQueueMetrics> metrics) {
		long totalTime = 0;
		long totalEvents = 0;

		for (EventQueueMetrics metric : metrics) {
			if (metric instanceof HistoricalEventQueueMetrics) {
				totalTime += ((HistoricalEventQueueMetrics) metric).elapsedTime;
				totalEvents += ((HistoricalEventQueueMetrics) metric).numOfEvents;
			}
		}

		if (totalEvents == 0) {
			return 0;
		} else {
			return totalEvents / ((double) totalTime / 1000000000);
		}

	}
}
