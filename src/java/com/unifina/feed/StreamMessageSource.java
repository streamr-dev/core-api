package com.unifina.feed;

import com.streamr.client.StreamrClient;
import com.streamr.client.authentication.ApiKeyAuthenticationMethod;
import com.streamr.client.options.StreamrClientOptions;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.rest.Stream;
import com.streamr.client.utils.StreamPartition;
import com.unifina.service.UserService;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
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
	 * @param globals
	 * @param consumer
	 * @param streamPartitions The set of StreamPartitions to subscribe to.
	 */
	public StreamMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		this.globals = globals;
		this.consumer = consumer;
		this.streamPartitions = streamPartitions;

		UserService userService = Holders.getApplicationContext().getBean(UserService.class);
		StreamrClientOptions options = new StreamrClientOptions(
			// Uses superpowers to get an API key for the user to authenticate the data
			new ApiKeyAuthenticationMethod(userService.getApiKeyForUser(globals.getUserId()))
		);

		options.setRestApiUrl(MapTraversal.getString(Holders.getConfig(), "streamr.api.http.url"));

		String wsUrl = MapTraversal.getString(Holders.getConfig(), "streamr.api.websocket.url");

		// TODO: Remove when Melchior adds this to the Java client
		if (!wsUrl.contains("controlLayerVersion") && !wsUrl.contains("messageLayerVersion")) {
			if (!wsUrl.contains("?")) {
				wsUrl += "?";
			}
			wsUrl += "&controlLayerVersion=1&messageLayerVersion=31";
		}

		options.setWebsocketApiUrl(wsUrl);
		// options.setWebsocketApiUrl(MapTraversal.getString(Holders.getConfig(), "streamr.api.websocket.url"));

		streamrClient = new com.streamr.client.StreamrClient(options);

		// Fetch Stream objects based on required StreamPartitions
		try {
			for (StreamPartition sp : streamPartitions) {
				if (!streamsByStreamId.containsKey(sp.getStreamId())) {
					Stream s = streamrClient.getStream(sp.getStreamId());
					streamsByStreamId.put(sp.getStreamId(), s);
				}
			}
		} catch (Exception e) {
			streamrClient.disconnect();
			throw new RuntimeException("Failed to subscribe to streams!", e);
		}
	}

	public void close() {
		streamrClient.disconnect();
		log.info("Closed Streamr connection to " + streamrClient.getOptions().getWebsocketApiUrl());
	}

	public abstract static class StreamMessageConsumer implements Consumer<StreamMessage> {
		/**
		 * Signals that there will be no further messages from the message source.
		 */
		public abstract void done();
	}

}
