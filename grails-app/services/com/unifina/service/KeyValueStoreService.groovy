package com.unifina.service

import com.unifina.utils.MapTraversal
import grails.compiler.GrailsCompileStatic
import grails.util.Holders
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.util.Assert

@GrailsCompileStatic
class KeyValueStoreService {

	private RedisClient redisClient
	StatefulRedisConnection<String, String> connection

	String get(String key) {
		return getConnection().sync().get(key)
	}

	String setWithExpiration(String key, String value, int seconds) {
		return getConnection().sync().setex(key, seconds, value)
	}

	void delete(String key) {
		getConnection().sync().del(key)
	}

	void resetExpiration(String key, Date date) {
		getConnection().sync().expireat(key, date)
	}

	private synchronized StatefulRedisConnection<String, String> getConnection() {
		if (connection == null) {
			List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
			String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

			Assert.notNull(hosts, "streamr.redis.hosts is null!")
			Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

			log.info("Configured Redis hosts: " + hosts)
			RedisURI redisURI = RedisURI.create("redis://" + hosts.get(0))
			if (password) {
				redisURI.setPassword(password)
			}

			log.info("Initializing RedisClient: " + redisURI)
			redisClient = RedisClient.create(redisURI)
			connection = redisClient.connect();
		}
		return connection
	}
}
