package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;

import java.io.Closeable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Represents a message source. Implementations should call consumer.accept(msg)
 * when messages are received.
 */
public abstract class StreamMessageSource implements Closeable {
	protected final Globals globals;
	protected final Consumer<StreamMessage> consumer;
	private final Collection<StreamPartition> streamPartitions;

	/**
	 * Creates an instance of this StreamMessageSource. The constructor should not block.
	 * Messages can be reported to the consumer as soon as they are available.
	 * @param globals
	 * @param consumer
	 * @param streamPartitions The set of StreamPartitions to subscribe to.
	 */
	public StreamMessageSource(Globals globals, Consumer<StreamMessage> consumer, Collection<StreamPartition> streamPartitions) {
		this.globals = globals;
		this.consumer = consumer;
		this.streamPartitions = streamPartitions;
	}

}
