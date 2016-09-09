package com.unifina.feed.redis;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractMessageSource;
import com.unifina.feed.MessageRecipient;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Creates multiple RedisMessageSources that connect to different hosts as defined config.
 * All the RedisMessageSources forward their received messages to the same recipient: the on
 * set on the MultipleRedisMessageSource instance.
 */
public class MultipleRedisMessageSource extends AbstractMessageSource<StreamrBinaryMessageRedis, String> {

	private static final Logger log = Logger.getLogger(MultipleRedisMessageSource.class);
	private final Map<String, RedisMessageSource> messageSourceByHost = new HashMap<>();
	private final Set<String> subscriptions = new HashSet<>();

	public MultipleRedisMessageSource(Feed feed, Map<String, Object> config) {
		super(feed, config);

		List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
		for (String host : hosts) {
			addHost(host);
		}
	}

	private void addHost(String host) {
		Map<String, Object> singleRedisConfig = new HashMap<>();
		singleRedisConfig.putAll(config);
		singleRedisConfig.put("host", host);
		RedisMessageSource messageSource = new RedisMessageSource(feed, singleRedisConfig);
		if (recipient != null) {
			messageSource.setRecipient(recipient);
		}
		messageSourceByHost.put(host, messageSource);
	}

	private void removeHost(String host) {
		RedisMessageSource messageSource = messageSourceByHost.remove(host);
		if (messageSource != null) {
			try {
				messageSource.close();
			} catch (IOException e) {
				log.error("RedisMessageSource threw exception while closing", e);
			}
		}
	}

	@Override
	public void subscribe(String key) {
		subscriptions.add(key);
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.subscribe(key);
		}
	}

	@Override
	public void unsubscribe(String key) {
		subscriptions.remove(key);
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.unsubscribe(key);
		}
	}

	@Override
	public void close() throws IOException {
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.close();
		}
	}

	@Override
	public void setRecipient(MessageRecipient<StreamrBinaryMessageRedis, String> recipient) {
		super.setRecipient(recipient);
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.setRecipient(recipient);
		}
	}
}
