package com.unifina.feed.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.codec.ByteArrayCodec;
import com.lambdaworks.redis.pubsub.RedisPubSubAdapter;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.feed.StreamMessageSource;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.function.Consumer;

public class RedisMessageSource extends StreamMessageSource {

	private static final Charset utf8 = Charset.forName("UTF-8");

	private final RedisClient client;
	private final RedisPubSubConnection<byte[], byte[]> connection;

	private static final Logger log = Logger.getLogger(RedisMessageSource.class);

	public RedisMessageSource(Globals globals, Consumer<StreamMessage> consumer, Collection<StreamPartition> streamPartitions, String host, String password) {
		super(globals, consumer, streamPartitions);
		RedisURI redisURI = RedisURI.create("redis://" + host);
		if (password != null) {
			redisURI.setPassword(password);
		}
		client = RedisClient.create(redisURI);
		log.info("Connecting to Redis on " + redisURI);
		connection = client.connectPubSub(new ByteArrayCodec());

		connection.addListener(new RedisPubSubAdapter<byte[], byte[]>() {
			@Override
			public void message(byte[] channel, byte[] messageBytes) {
				try {
					StreamMessage msg = StreamMessage.fromBytes(messageBytes);
					consumer.accept(msg);
				} catch (IOException e) {
					log.error(e);
				}
			}

			@Override
			public void subscribed(byte[] channel, long count) {
				String channelAsString = new String(channel, utf8);
				log.info("Subscribed " + channelAsString + " on host " + host);
			}

			@Override
			public void unsubscribed(byte[] channel, long count) {
				String channelAsString = new String(channel, utf8);
				log.info("Unsubscribed " + channelAsString + " on host " + host);
			}
		});

		for (StreamPartition sp : streamPartitions) {
			connection.subscribe(streamPartitionToChannelBytes(sp));
		}
	}

	private byte[] streamPartitionToChannelBytes(StreamPartition streamPartition) {
		String key = streamPartition.getStreamId() + "-" + streamPartition.getPartition();
		return key.getBytes(utf8);
	}

	@Override
	public void close() {
		connection.close();
		client.shutdown();
	}

}
