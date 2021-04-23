package com.unifina.service

import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.RedisURI
import com.unifina.utils.ApplicationConfig
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.springframework.util.Assert
import spock.lang.Specification

import java.time.Instant

class KeyValueStoreServiceIntegrationSpec extends Specification {
	KeyValueStoreService service
	RedisConnection<String, String> connection

	void setup() {
		service = Holders.getApplicationContext().getBean(KeyValueStoreService)

		List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
		String password = ApplicationConfig.getString("streamr.redis.password");

		Assert.notNull(hosts, "streamr.redis.hosts is null!")
		Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

		RedisClient redisClient = RedisClient.create(RedisURI.create("redis://" + password + "@" + hosts.get(0)))
		connection = redisClient.connect()
	}

	void "get() returns inserted value"() {
		String key = "key1"
		String value = "value1"
		when:
		connection.set(key, value)
		then:
		service.get(key) == value
	}

	void "setWithExpiration() inserts key and value with expiration"() {
		String key = "key2"
		String value = "value2"
		when:
		service.setWithExpiration(key, value, 1)
		then:
		connection.get(key) == value
		Thread.sleep(1000)
		connection.get(key) == null
	}

	void "delete() removes key"() {
		String key = "key3"
		String value = "value3"
		when:
		connection.set(key, value)
		service.delete(key)
		Thread.sleep(500)
		then:
		connection.get(key) == null
	}

	void "resetExpiration() resets expiration of key"() {
		String key = "key4"
		String value = "value4"
		connection.setex(key, 10, value)
		Long ttl1 = connection.ttl(key)
		Thread.sleep(2000)
		when:
		service.resetExpiration(key, Date.from(Instant.now().plusSeconds(10)))
		then:
		Long ttl2 = connection.ttl(key)
		ttl2 - ttl1 <= 10
	}
}
