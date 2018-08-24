package com.unifina.data;

public class EventQueueMetrics {
	private long elapsedTime = 0;
	private long numOfEvents = 0;
	private long sumOfDelays = 0;

	void countEvent(long timeDiff, long delay) {
		elapsedTime += timeDiff;
		sumOfDelays += delay;
		numOfEvents++;
	}

	public static double aggregateEventsPerSecond(Iterable<EventQueueMetrics> metrics) {
		long totalTime = 0;
		long totalEvents = 0;

		for (EventQueueMetrics metric : metrics) {
			totalTime += metric.elapsedTime;
			totalEvents += metric.numOfEvents;
		}

		if (totalEvents == 0) {
			return 0;
		} else {
			return totalEvents / (double) totalTime;
		}

	}

	public static double aggregateMeanProcessingDelay(Iterable<EventQueueMetrics> metrics) {
		long totalDelay = 0;
		long totalEvents = 0;

		for (EventQueueMetrics metric : metrics) {
			totalDelay += metric.sumOfDelays;
			totalEvents += metric.numOfEvents;
		}

		if (totalEvents == 0) {
			return 0;
		} else {
			return totalDelay / (double) totalEvents;
		}
	}
}
