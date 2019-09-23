package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;

import java.io.Closeable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Represents a message source. Implementations should call consumer.accept(msg)
 * when messages are received.
 */
public abstract class StreamMessageSource implements Closeable {
	protected final StreamMessageConsumer consumer;
	protected final Collection<StreamPartition> streamPartitions;

	/**
	 * Creates an instance of this StreamMessageSource. The constructor should not block.
	 * Messages can be reported to the consumer as soon as they are available.
	 * @param consumer
	 * @param streamPartitions The set of StreamPartitions to subscribe to.
	 */
	public StreamMessageSource(StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		this.consumer = consumer;
		this.streamPartitions = streamPartitions;
	}

	public abstract static class StreamMessageConsumer implements Consumer<StreamMessage> {
		/**
		 * Signals that there will be no further messages from the message source.
		 */
		public abstract void done();
	}

}
