package com.unifina.data;

public class EventQueueMetrics {
	private long elapsedTime = 0;
	private long numOfEvents = 0;
	private long sumOfDelays = 0;

	public void countEvent(long timeDiff, long delay) {
		elapsedTime += timeDiff;
		sumOfDelays += delay;
		numOfEvents++;
	}

	public double eventsPerSecond() {
		return numOfEvents / ((double) elapsedTime / 1000000000);
	}

	public double meanProcessingDelay() {
		return ((double) sumOfDelays / 1000) / (double) numOfEvents;
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
			return totalEvents / ((double) totalTime / 1000000000);
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
			return ((double) totalDelay / 1000) / (double) totalEvents;
		}
	}

	@Override
	public String toString() {
		return String.format("[EventQueueMetrics: events: %s, eventsPerSecond: %s, meanProcessingDelay: %s", numOfEvents, eventsPerSecond(), meanProcessingDelay());
	}
}
