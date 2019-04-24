package com.unifina.datasource;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.data.HistoricalEventQueue;
import com.unifina.feed.StreamMessageSource;
import com.unifina.feed.cassandra.CassandraMessageSource;
import com.unifina.utils.Globals;

import java.util.Collection;
import java.util.function.Consumer;

public class HistoricalDataSource extends DataSource {

	public HistoricalDataSource(Globals globals) {
		super(globals);
	}

	@Override
	protected StreamMessageSource createStreamMessageSource(Collection<StreamPartition> streamPartitions, Consumer<StreamMessage> consumer) {
		return new CassandraMessageSource(globals, consumer, streamPartitions);
	}

	@Override
	protected DataSourceEventQueue createEventQueue() {
		return new HistoricalEventQueue(globals, this);
	}

}
