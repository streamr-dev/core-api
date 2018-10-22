package com.unifina.service

import com.lambdaworks.redis.RedisAsyncConnection
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisURI
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.springframework.util.Assert

class KeyValueStoreService {

	private RedisClient redisClient
	private RedisAsyncConnection<String, String> connection

	String get(String key) {
		return getConnection().get(key).get()
	}

	String setWithExpiration(String key, String value, int seconds) {
		return getConnection().setex(key, seconds, value).get()
	}

	void delete(String key) {
		getConnection().del(key)
	}

	void resetExpiration(String key, Date date) {
		getConnection().expireat(key, date)
	}

	private RedisAsyncConnection<String, String> getConnection() {
		if (connection == null) {
			List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
			String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

			Assert.notNull(hosts, "streamr.redis.hosts is null!")
			Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

			redisClient = RedisClient.create(RedisURI.create("redis://" + password + "@" + hosts.get(0)))
			connection = redisClient.connectAsync()
		}
		return connection
	}
}
