package com.unifina.datasource;


import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.data.ClockTickEvent;
import com.unifina.data.RealtimeEventQueue;
import com.unifina.feed.StreamMessageSource;
import com.unifina.feed.redis.MultipleRedisMessageSource;
import com.unifina.serialization.SerializationRequest;
import com.unifina.service.SerializationService;
import com.unifina.signalpath.SignalPath;
import com.unifina.utils.Globals;
import grails.util.Holders;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

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
												 final ClockTickEvent event = new ClockTickEvent(new Date());
												 accept(event);
											 }
										 },
				new Date(now.getTime() + (1000 - (now.getTime() % 1000))), // Time till next even second
				1000);   // Repeat every second

			// Schedule serialization events
			SerializationService serializationService = Holders.getApplicationContext().getBean(SerializationService.class);
			long serializationIntervalInMs = serializationService.serializationIntervalInMillis();

			if (serializationIntervalInMs > 0) {
				for (final SignalPath signalPath : getSerializableSignalPaths()) {
					secTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							accept(SerializationRequest.makeFeedEvent(signalPath));
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
	protected StreamMessageSource createStreamMessageSource(Collection<StreamPartition> streamPartitions, Consumer<StreamMessage> consumer) {
		return new MultipleRedisMessageSource(globals, consumer, streamPartitions);
	}

	@Override
	protected DataSourceEventQueue createEventQueue() {
		return new RealtimeEventQueue(globals, this);
	}

}
