package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.AbstractStreamSourceModule;
import com.unifina.utils.Globals;

import java.util.Collection;

public abstract class StreamEventRecipient extends AbstractEventRecipient<AbstractStreamSourceModule, StreamMessage> {

	private final Stream stream;
	private final Collection<StreamPartition> partitions;

	public StreamEventRecipient(Globals globals, Stream stream, Collection<StreamPartition> partitions) {
		super(globals);
		this.stream = stream;
		this.partitions = partitions;
	}

	public Stream getStream() {
		return stream;
	}

	public Collection<StreamPartition> getPartitions() {
		return partitions;
	}

}
