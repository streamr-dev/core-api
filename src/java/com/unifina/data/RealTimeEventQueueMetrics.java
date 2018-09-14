package com.unifina.data;

public class RealTimeEventQueueMetrics implements EventQueueMetrics {
	private long elapsedTime = 0;
	private long numOfEvents = 0;
	private long sumOfDelays = 0;

	@Override
	public void countEvent(long timeDiff, long delay) {
		elapsedTime += timeDiff;
		sumOfDelays += delay;
		numOfEvents++;
	}

	public static double aggregateEventsPerSecond(Iterable<EventQueueMetrics> metrics) {
		long totalTime = 0;
		long totalEvents = 0;

		for (EventQueueMetrics metric : metrics) {
			if (metric instanceof RealTimeEventQueueMetrics) {
				totalTime += ((RealTimeEventQueueMetrics) metric).elapsedTime;
				totalEvents += ((RealTimeEventQueueMetrics) metric).numOfEvents;
			}
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
			if (metric instanceof RealTimeEventQueueMetrics) {
				totalDelay += ((RealTimeEventQueueMetrics) metric).sumOfDelays;
				totalEvents += ((RealTimeEventQueueMetrics) metric).numOfEvents;

			}
		}

		if (totalEvents == 0) {
			return 0;
		} else {
			return ((double) totalDelay / 1000) / (double) totalEvents;
		}
	}
}
