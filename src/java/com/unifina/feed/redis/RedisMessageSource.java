package com.unifina.feed.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.codec.ByteArrayCodec;
import com.lambdaworks.redis.pubsub.RedisPubSubAdapter;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractMessageSource;
import com.unifina.feed.Message;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RedisMessageSource extends AbstractMessageSource<StreamMessage, String> {

	private static final Charset utf8 = Charset.forName("UTF-8");

	private final String host;
	private final RedisClient client;
	private final RedisPubSubConnection<byte[], byte[]> connection;

	private static final Logger log = Logger.getLogger(RedisMessageSource.class);

	public RedisMessageSource(Feed feed, Map<String, Object> config) {
		super(feed, config);

		if (!config.containsKey("host")) {
			throw new IllegalArgumentException("Redis config map does not contain the host key!");
		}

		host = config.get("host").toString();
		RedisURI redisURI = RedisURI.create("redis://" + host);
		if (config.containsKey("password")) {
			redisURI.setPassword("" + config.get("password"));
		}
		client = RedisClient.create(redisURI);
		log.info("Connecting to Redis on " + redisURI);
		connection = client.connectPubSub(new ByteArrayCodec());

		connection.addListener(new RedisPubSubAdapter<byte[], byte[]>() {
			@Override
			public void message(byte[] channel, byte[] messageBytes) {
				String streamId = new String(channel, utf8);
				try {
					StreamMessage msg = StreamMessage.fromJson(new String(messageBytes, StandardCharsets.UTF_8));
					forward(new Message<>(streamId, msg));
				} catch (IOException e) {
					log.error(e);
				}
			}

			@Override
			public void subscribed(byte[] channel, long count) {
				String streamId = new String(channel, utf8);
				log.info("Subscribed " + streamId + " on host " + host);
			}

			@Override
			public void unsubscribed(byte[] channel, long count) {
				String streamId = new String(channel, utf8);
				log.info("Unsubscribed " + streamId + " on host " + host);
			}
		});
	}

	@Override
	public void subscribe(String key) {
		connection.subscribe(key.getBytes(utf8));
	}

	@Override
	public void unsubscribe(String key) {
		connection.unsubscribe(key.getBytes(utf8));
	}

	@Override
	public void close() throws IOException {
		connection.close();
		client.shutdown();
	}

}
