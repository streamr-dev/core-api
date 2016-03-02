package com.unifina.feed;

import com.unifina.domain.data.Feed;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstraction for polling message sources. Subclasses must be implement createPoller(subscriber), a method
 * that returns a Poller. A Poller is a Runnable whose run() method gets called at fixed intervals.
 * This abstraction also takes care of closing individual Pollers.
 */
public abstract class PollingMessageSource<RawMessageClass, KeyClass> extends AbstractMessageSource<RawMessageClass, KeyClass> {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final Map<KeyClass, ScheduledFuture<?>> handleByKey = new HashMap<>();
	private final Map<KeyClass, Poller> pollerByKey = new HashMap<>();

	private static final Logger log = Logger.getLogger(PollingMessageSource.class);

	public PollingMessageSource(Feed feed, Map<String, Object> config) {
		super(feed, config);
	}

	@Override
	public void subscribe(KeyClass subscriber) {
		final PollingMessageSource<RawMessageClass, KeyClass> me = this;
		if (!handleByKey.containsKey(subscriber)) {
			final KeyClass sub = subscriber;
			final Poller poller = createPoller(subscriber);
			final Runnable catchingPollerWrapper = new Runnable() {
				@Override
				public void run() {
					try {
						List<Message<RawMessageClass, KeyClass>> messages = poller.poll();
						for (Message<RawMessageClass, KeyClass> msg : messages) {
							me.forward(msg);
						}
					} catch (Throwable t) {
						log.error("Error while polling for data for subscriber: "+sub, t);
					}
				}
			};
			ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(catchingPollerWrapper, 0, poller.getPollInterval(), TimeUnit.MILLISECONDS);
			handleByKey.put(subscriber, handle);
			pollerByKey.put(subscriber, poller);
		}
	}

	@Override
	public void unsubscribe(KeyClass subscriber) {
		ScheduledFuture<?> handle = handleByKey.remove(subscriber);
		Poller poller = pollerByKey.remove(subscriber);
		if (handle != null) {
			handle.cancel(true);
		}
		if (poller != null) {
			poller.close();
		}
	}

	@Override
	public void close() throws IOException {
		for (ScheduledFuture<?> handle : handleByKey.values()) {
			handle.cancel(true);
		}
		handleByKey.clear();

		for (Poller poller : pollerByKey.values()) {
			poller.close();
		}
		pollerByKey.clear();
	}

	protected abstract Poller createPoller(KeyClass subscriber);
}
