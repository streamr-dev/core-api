package com.unifina.feed;

import com.unifina.data.Event;
import org.apache.log4j.Logger;

import java.util.function.Function;

/**
 * Logs a warning if the seen FeedEvents are older than a threshold.
 * Returns the same event.
 */
public class AgeChecker implements Function<Event, Event> {
	private static final Logger log = Logger.getLogger(AgeChecker.class);

	int thresholdMillis;

	public AgeChecker() {
		this(1000);
	}

	public AgeChecker(int thresholdMillis) {
		this.thresholdMillis = thresholdMillis;
	}

	@Override
	public Event apply(Event event) {
		// Warn about old events
		if (event != null) {
			long age = System.currentTimeMillis() - event.getTimestamp().getTime();
			if (age > thresholdMillis) {
				log.warn("Event age " + age + ": " + event);
			}
		}
		return event;
	}
}
