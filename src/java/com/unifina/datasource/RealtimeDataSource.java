package com.unifina.datasource;


import com.streamr.client.utils.StreamPartition;
import com.unifina.data.ClockTick;
import com.unifina.data.Event;
import com.unifina.feed.RealtimeMessageSource;
import com.unifina.feed.StreamMessageSource;
import com.unifina.serialization.SerializationRequest;
import com.unifina.service.SerializationService;
import com.unifina.signalpath.SignalPath;
import com.unifina.utils.Globals;
import grails.util.Holders;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RealtimeDataSource extends DataSource {

	private final Timer secTimer = new Timer();

	public RealtimeDataSource(Globals globals) {
		super(globals);

		// Schedule some timed events on start
		addStartListener(() -> {
			final Date now = new Date();
			secTimer.scheduleAtFixedRate(new TimerTask() {
											 @Override
											 public void run() {
												 final ClockTick tick = new ClockTick(new Date());
												 enqueue(new Event<>(tick, tick.getTimestampAsDate(), 0L, null));
											 }
										 },
				new Date(now.getTime() + (1000 - (now.getTime() % 1000))), // First run on next even second
				1000);   // Repeat every second

			// Schedule serialization events
			SerializationService serializationService = Holders.getApplicationContext().getBean(SerializationService.class);
			long serializationIntervalInMs = serializationService.serializationIntervalInMillis();

			if (serializationIntervalInMs > 0) {
				for (final SignalPath signalPath : getSerializableSignalPaths()) {
					secTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							enqueue(SerializationRequest.makeFeedEvent(signalPath));
						}
					}, serializationIntervalInMs, serializationIntervalInMs);
				}
			}
		});

		// Cleanup timed events on stop
		addStopListener(() -> {
			secTimer.cancel();
			secTimer.purge();
		});
	}

	@Override
	protected StreamMessageSource createStreamMessageSource(Collection<StreamPartition> streamPartitions, StreamMessageSource.StreamMessageConsumer consumer) {
		return new RealtimeMessageSource(globals, consumer, streamPartitions);
	}

	@Override
	protected DataSourceEventQueue createEventQueue() {
		return new RealtimeEventQueue(globals, this);
	}

}
