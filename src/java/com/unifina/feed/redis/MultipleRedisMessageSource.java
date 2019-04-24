package com.unifina.feed.redis;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.feed.StreamMessageSource;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Creates multiple RedisMessageSources that connect to different hosts as defined config.
 * All the RedisMessageSources onMessage their received messages to the same recipient: the one
 * set on the MultipleRedisMessageSource instance.
 */
public class MultipleRedisMessageSource extends StreamMessageSource {
	private final Map<String, RedisMessageSource> messageSourceByHost = new HashMap<>();

	public MultipleRedisMessageSource(Globals globals, Consumer<StreamMessage> consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
		String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

		Assert.notNull(hosts, "streamr.redis.hosts is null!");
		Assert.notEmpty(hosts, "streamr.redis.hosts is empty!");
		Assert.notNull(password, "streamr.redis.password is null!");

		for (String host : hosts) {
			RedisMessageSource messageSource = new RedisMessageSource(globals, consumer, streamPartitions, host, password);
			messageSourceByHost.put(host, messageSource);
		}
	}

	@Override
	public void close() {
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.close();
		}
	}
}
