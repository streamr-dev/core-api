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
 * All the RedisMessageSources forward their received messages to the same recipient: the one
 * set on the MultipleRedisMessageSource instance.
 */
public class MultipleRedisMessageSource extends AbstractMessageSource<StreamrBinaryMessageWithKafkaMetadata, String> {
	private final Map<String, RedisMessageSource> messageSourceByHost = new HashMap<>();

	public MultipleRedisMessageSource(Feed feed, Map<String, Object> config) {
		super(feed, config);

		List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
		String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");
		for (String host : hosts) {
			addHost(host, password);
		}
	}

	private void addHost(String host, String password) {
		Map<String, Object> singleRedisConfig = new HashMap<>();
		singleRedisConfig.putAll(getConfig());
		singleRedisConfig.put("host", host);
		singleRedisConfig.put("password", password);
		RedisMessageSource messageSource = new RedisMessageSource(getFeed(), singleRedisConfig);
		if (getRecipient() != null) {
			messageSource.setRecipient(getRecipient());
		}
		messageSourceByHost.put(host, messageSource);
	}

	@Override
	public void subscribe(String key) {
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.subscribe(key);
		}
	}

	@Override
	public void unsubscribe(String key) {
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
	public void setRecipient(MessageRecipient<StreamrBinaryMessageWithKafkaMetadata, String> recipient) {
		super.setRecipient(recipient);
		for (RedisMessageSource ms : messageSourceByHost.values()) {
			ms.setRecipient(recipient);
		}
	}
}
