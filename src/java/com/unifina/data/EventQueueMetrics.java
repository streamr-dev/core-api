package com.unifina.data;

public interface EventQueueMetrics {
	void countEvent(long timeDiff, long delay);
}
