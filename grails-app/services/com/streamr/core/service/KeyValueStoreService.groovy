package com.streamr.core.service
import com.lambdaworks.redis.RedisAsyncConnection
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisURI
import com.streamr.core.utils.ApplicationConfig
import com.streamr.core.utils.MapTraversal
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

	private synchronized RedisAsyncConnection<String, String> getConnection() {
		if (connection == null) {
			List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
			String password = ApplicationConfig.getString("streamr.redis.password");

			Assert.notNull(hosts, "streamr.redis.hosts is null!")
			Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

			log.info("Configured Redis hosts: " + hosts)
			RedisURI redisURI = RedisURI.create("redis://" + hosts.get(0))
			if (password) {
				redisURI.setPassword(password)
			}

			log.info("Initializing RedisClient: " + redisURI)
			redisClient = RedisClient.create(redisURI)
			connection = redisClient.connectAsync()
		}
		return connection
	}
}
