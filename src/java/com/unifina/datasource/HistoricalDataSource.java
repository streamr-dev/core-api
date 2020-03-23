package com.unifina.datasource;

import com.streamr.client.utils.StreamPartition;
import com.unifina.data.HistoricalEventQueue;
import com.unifina.feed.HistoricalMessageSource;
import com.unifina.feed.StreamMessageSource;
import com.unifina.utils.Globals;

import java.util.Collection;

public class HistoricalDataSource extends DataSource {

	public HistoricalDataSource(Globals globals) {
		super(globals);
	}

	@Override
	protected StreamMessageSource createStreamMessageSource(Collection<StreamPartition> streamPartitions, StreamMessageSource.StreamMessageConsumer consumer) {
		return new HistoricalMessageSource(globals, consumer, streamPartitions);
	}

	@Override
	protected DataSourceEventQueue createEventQueue() {
		return new HistoricalEventQueue(globals, this);
	}

}
