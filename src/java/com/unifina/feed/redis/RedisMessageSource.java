package com.unifina.feed.redis;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractMessageSource;
import org.apache.log4j.Logger;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

public class RedisMessageSource extends AbstractMessageSource<StreamrBinaryMessageRedis, String> {

	private static final Charset utf8 = Charset.forName("UTF-8");

	private final Jedis jedis;
	private final BinaryJedisPubSub handler;
	private final String host;

	private static final Logger log = Logger.getLogger(RedisMessageSource.class);

	public RedisMessageSource(Feed feed, Map<String, Object> config) {
		super(feed, config);

		if (!config.containsKey("host")) {
			throw new IllegalArgumentException("Redis config map does not contain the host key!");
		}

		host = config.get("host").toString();
		jedis = new Jedis(host);
		handler = new BinaryJedisPubSub() {
			public void onMessage(byte[] channel, byte[] messageBytes) {
				String streamId = new String(channel, utf8);
				StreamrBinaryMessageRedis msg = new StreamrBinaryMessageRedis(ByteBuffer.wrap(messageBytes));
				forward(msg, streamId, msg.getOffset(), false);
			}

			public void onSubscribe(byte[] channel, int subscribedChannels) {
				String streamId = new String(channel, utf8);
				log.info("Subscribed "+streamId+" on host "+host);
			}

			public void onUnsubscribe(byte[] channel, int subscribedChannels) {
				String streamId = new String(channel, utf8);
				log.info("Unsubscribed "+streamId+" on host "+host);
			}
		};
	}

	@Override
	public void subscribe(String key) {
		jedis.subscribe(handler, key.getBytes(utf8));
	}

	@Override
	public void unsubscribe(String key) {
		handler.unsubscribe(key.getBytes(utf8));
	}

	@Override
	public void close() throws IOException {
		jedis.quit();
	}
}
