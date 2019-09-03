package com.unifina.feed;

import com.streamr.client.StreamrClient;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.rest.Stream;
import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Represents a message source. Implementations should call consumer.accept(msg)
 * when messages are received.
 */
public abstract class StreamMessageSource implements Closeable {

	private static final Logger log = Logger.getLogger(StreamMessageSource.class);

	protected final Globals globals;
	protected final StreamMessageConsumer consumer;
	protected final Collection<StreamPartition> streamPartitions;

	protected final StreamrClient streamrClient;
	protected final HashMap<String, Stream> streamsByStreamId = new HashMap<>();

	/**
	 * Creates an instance of this StreamMessageSource. The constructor should not block.
	 * Messages can be reported to the consumer as soon as they are available.
	 * @param consumer
	 * @param streamPartitions The set of StreamPartitions to subscribe to.
	 */
	public StreamMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		this.globals = globals;
		this.consumer = consumer;
		this.streamPartitions = streamPartitions;

		// Use the shared StreamrClient instance connected to this run context
		streamrClient = globals.getStreamrClient();

		log.info("Using StreamrClient configured with websocket url " + streamrClient.getOptions().getWebsocketApiUrl());

		// Fetch Stream objects based on required StreamPartitions
		try {
			for (StreamPartition sp : streamPartitions) {
				if (!streamsByStreamId.containsKey(sp.getStreamId())) {
					Stream s = streamrClient.getStream(sp.getStreamId());
					streamsByStreamId.put(sp.getStreamId(), s);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to subscribe to streams!", e);
		}
	}

	public abstract void close();

	public abstract static class StreamMessageConsumer implements Consumer<StreamMessage> {
		/**
		 * Signals that there will be no further messages from the message source.
		 */
		public abstract void done();
	}

}
