package com.unifina.datasource;

import com.unifina.data.Event;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

public class RealtimeEventQueue extends DataSourceEventQueue {

	private static final long AGE_WARNING_THRESHOLD_MS = 1000;

	private static final Logger log = Logger.getLogger(RealtimeEventQueue.class);

	public RealtimeEventQueue(Globals globals, DataSource dataSource) {
		super(globals, dataSource, DEFAULT_CAPACITY, true);
	}

	@Override
	protected void beforeEvent(Event event) {
		// Warn about old events
		if (event != null) {
			long age = System.currentTimeMillis() - event.getTimestamp().getTime();
			if (age > AGE_WARNING_THRESHOLD_MS) {
				log.warn("Event age " + age + ": " + event);
			}
		}
	}
}
