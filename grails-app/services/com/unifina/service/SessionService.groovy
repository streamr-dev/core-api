package com.unifina.service

import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.RedisURI
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.springframework.util.Assert

class SessionService {
	static final int TOKEN_LENGTH = 64
	static final int TTL_HOURS = 3

	RedisClient redisClient

	SessionToken generateToken(SecUser user) {
		SessionToken sk = new SessionToken(TOKEN_LENGTH, user, TTL_HOURS)
		RedisConnection<String, String> connection = getRedisClient().connect()
		connection.setex(sk.getToken(), TTL_HOURS * 3600, sk.getUser().id.toString())
		return sk
	}

	SecUser getUserFromToken(String token) {
		RedisConnection<String, String> connection = getRedisClient().connect()
		String userID = connection.get(token)
		if (userID == null) {
			return null
		}
		return SecUser.get(userID.toLong())
	}

	private RedisClient getRedisClient() {
		if (redisClient == null) {
			List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
			String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

			Assert.notNull(hosts, "streamr.redis.hosts is null!")
			Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")
			Assert.notNull(password, "streamr.redis.password is null!")

			redisClient = RedisClient.create(RedisURI.create("redis://" + password + "@" + hosts.get(0)))
		} else {
			return redisClient
		}
	}

}
